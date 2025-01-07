package ads.mediastreet.ai.service;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Binder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;

import com.clover.cfp.connector.CustomActivityListener;
import com.clover.cfp.connector.CustomActivityRequest;
import com.clover.cfp.connector.CustomActivityResponse;
import com.clover.cfp.connector.MessageFromActivity;
import com.clover.cfp.connector.MessageToActivity;
import com.clover.cfp.connector.RemoteDeviceConnector;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.connector.IDisplayConnector;
import com.clover.sdk.v3.inventory.InventoryContract;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.OrderContract;
import com.clover.sdk.v3.order.OrderV31Connector;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import ads.mediastreet.ai.R;
import ads.mediastreet.ai.repositories.CreateOrderRespository;
import ads.mediastreet.ai.utils.DeviceUtils;

public class OrderListenerService extends Service {

    private static final String TAG = "OrderListenerService";
    private OrderV31Connector mOrderConnector;
    private OrderConnector.OnOrderUpdateListener2 mOrderUpdateListener;
    private RemoteDeviceConnector remoteDeviceConnector;
    private IDisplayConnector mDisplayConnector;
    private MerchantConnector mMerchantConnector;
    private String merchantId;
    private Map<String, Item> itemMap = new HashMap<>();
    HandlerThread backgroundThread;
    Handler backgroundHandler;
    private String currentOrderId;
    private Set<String> currentItemSet = new HashSet<>();

    public interface OnMerchantIdReadyListener {
        void onMerchantIdReady(String merchantId);
    }

    private OnMerchantIdReadyListener merchantIdReadyListener;

    public void setOnMerchantIdReadyListener(OnMerchantIdReadyListener listener) {
        this.merchantIdReadyListener = listener;
        // If merchantId is already available, notify immediately
        merchantId = "TEST";
        if (merchantId != null) {
            listener.onMerchantIdReady(merchantId);
        }
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public OrderListenerService getService() {
            return OrderListenerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        backgroundThread = new HandlerThread("OrderListenerServiceThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        initializeOrderConnector();
        initializeMerchantConnector();
        startForegroundService();
        fetchInventoryItems();
    }

    private void initializeOrderConnector() {
        mOrderUpdateListener = new OrderV31Connector.OnOrderUpdateListener2() {

            @Override
            public void onOrderUpdated(String orderId, boolean selfChange) {
                Log.d(TAG, "Order Updated: " + orderId);
            }

            @Override
            public void onOrderCreated(String orderId) {
                Log.d(TAG, "Order Created: " + orderId);
                currentOrderId = orderId;
                currentItemSet.clear(); // Initialize item set for the new order

                if (remoteDeviceConnector != null) {
                    String action = "ads.mediastreet.ai.activity.CFP";
                    String payload = "FINISH"; // No additional data needed
                    sendMessageToCustomerScreen(action, payload);
                }
            }

            @Override
            public void onOrderDeleted(String orderId) {
                Log.d(TAG, "Order deleted: " + orderId);
            }

            @Override
            public void onLineItemsAdded(String orderId, List<String> lineItemIds) {
                for(String item:lineItemIds)
                    Log.d(TAG, "Added Item " + item);
                currentItemSet.addAll(lineItemIds); // Add new line items
            }

            @Override
            public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
                for(String item:lineItemIds)
                    Log.d(TAG, "Updated Item " + item);
                currentItemSet.addAll(lineItemIds); // Update line items
            }

            @Override
            public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
                for(String item:lineItemIds)
                    Log.d(TAG, "Deleted Item " + item);
                currentItemSet.removeAll(lineItemIds); // Remove line items
            }

            @Override
            public void onPaymentProcessed(String orderId, String paymentId) {
                Log.d(TAG, "Payment Processed " + paymentId);
                handleOrderUpdate(orderId);
                // Clear the current order and item set after processing payment
                currentOrderId = null;
                currentItemSet.clear();
            }

            @Override
            public void onOrderDiscountAdded(String orderId, String discountId) {

            }

            @Override
            public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {

            }

            @Override
            public void onLineItemModificationsAdded(String orderId, List<String> lineItemIds, List<String> modificationIds) {

            }

            @Override
            public void onLineItemDiscountsAdded(String orderId, List<String> lineItemIds, List<String> discountIds) {

            }

            @Override
            public void onLineItemExchanged(String orderId, String oldLineItemId, String newLineItemId) {

            }

            @Override
            public void onRefundProcessed(String orderId, String refundId) {

            }

            @Override
            public void onCreditProcessed(String orderId, String creditId) {

            }
        };
        mOrderConnector = new OrderV31Connector(this, CloverAccount.getAccount(this), null);
        mOrderConnector.connect();
        mOrderConnector.addOnOrderChangedListener(mOrderUpdateListener);
    }

    private void initializeMerchantConnector() {
        mMerchantConnector = new MerchantConnector(this, CloverAccount.getAccount(this), null);
        mMerchantConnector.getMerchant(new ServiceConnector.Callback<Merchant>() {
            @Override
            public void onServiceSuccess(Merchant result, ResultStatus status) {
                merchantId = result.getId();
                Log.d(TAG, "Merchant ID: " + merchantId);
                if (merchantIdReadyListener != null) {
                    merchantIdReadyListener.onMerchantIdReady(merchantId);
                }
            }
            @Override
            public void onServiceFailure(ResultStatus status) {
                Log.e(TAG, "Failed to get merchant information: " + status);
            }

            @Override
            public void onServiceConnectionFailure() {
                Log.e(TAG, "Service connection failure");
            }

        });
    }

    private void fetchInventoryItems() {
        backgroundHandler.post(() -> {
            try {
                // Query all items using InventoryContract
                Cursor cursor = getContentResolver().query(
                    InventoryContract.Item.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                );

                if (cursor != null) {
                    try {
                        // Get column indices once before the loop
                        int idColumnIndex = cursor.getColumnIndex(InventoryContract.Item.ID);
                        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.Item.NAME);
                        int skuColumnIndex = cursor.getColumnIndex(InventoryContract.Item.SKU);
                        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.Item.PRICE);

                        // Validate column indices
                        if (idColumnIndex < 0 || nameColumnIndex < 0 || skuColumnIndex < 0 || priceColumnIndex < 0) {
                            Log.e(TAG, "Required columns not found in cursor");
                            return;
                        }

                        while (cursor.moveToNext()) {
                            String id = cursor.getString(idColumnIndex);
                            String name = cursor.getString(nameColumnIndex);
                            String sku = cursor.getString(skuColumnIndex);
                            long price = cursor.getLong(priceColumnIndex);
                            
                            // Create Item object and add to map
                            Item item = new Item();
                            item.setId(id);
                            item.setName(name);
                            item.setSku(sku);
                            item.setPrice(price);
                            
                            itemMap.put(id, item);
                            Log.d(TAG, "Loaded item: " + name + " (ID: " + id + ")");
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching inventory items", e);
            }
        });
    }

    private void handleOrderUpdate(String orderId) {
        backgroundHandler.post(() -> {
            Log.d(TAG, "Payment complete !!");
            Account account = CloverAccount.getAccount(this);
            String activityAction = "ads.mediastreet.ai.activity.CFP";
            String deviceId = DeviceUtils.INSTANCE.getDeviceId(this);

            List<String> products = new ArrayList<>();

            try {
                // Get a fresh copy of the order
                Order order = mOrderConnector.getOrder(orderId);
                if (order == null) {
                    Log.e(TAG, "Order is null for orderId: " + orderId);
                    return;
                }

                List<LineItem> lineItems = order.getLineItems();
                if (lineItems == null) {
                    Log.e(TAG, "Line items are null for orderId: " + orderId);
                    return;
                }

                for (LineItem lineItem : lineItems) {
                    if (lineItem != null && lineItem.getItem() != null) {
                        String itemId = lineItem.getItem().getId();
                        String itemName = lineItem.getName();
                        Log.d(TAG, "Line Item: ID=" + itemId + ", Name=" + itemName);
                        products.add(itemName);
                    } else {
                        Log.e(TAG, "LineItem or its item is null");
                    }
                }

                double total = order.getTotal() / 100.0; // Convert from cents to dollars
                Log.d(TAG, "Order Total: $" + total);

                AtomicReference<String> payload = new AtomicReference<>("{\"message\":\"Welcome to our store!\"}");
                CreateOrderRespository.createOrder(merchantId, orderId, total, products, orderResponse -> {
                    if (orderResponse != null) {
                        payload.set(new Gson().toJson(orderResponse));
                        Log.d(TAG, "Order created successfully: " + payload.get());
                        startCustomerFacingActivity(activityAction, payload.get());
                    } else {
                        Log.e(TAG, "Failed to create order - response is null");
                    }
                    return null;
                });

            } catch (RemoteException | ClientException | ServiceException | BindingException e) {
                Log.e(TAG, "Error getting order details in handleOrderUpdate", e);
            }
        });
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        // Create a persistent notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("Listening for Orders")
                .setContentText("Order listener is running in the background")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .build();
        startForeground(1, notification);  // Start the service in the foreground with the notification
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        backgroundHandler.post(() -> {
            if (remoteDeviceConnector == null) {
                remoteDeviceConnector = new RemoteDeviceConnector(getApplicationContext(), CloverAccount.getAccount(this));
                remoteDeviceConnector.connect();
            }
        });

        return START_STICKY;  // Keeps the service running until explicitly stopped
    }

    public void sendMessageToCustomerScreen(String action, String payload) {
        backgroundHandler.post(() -> {
            if (remoteDeviceConnector != null) {
                try {
                    MessageToActivity messageToActivity = new MessageToActivity(action, payload);
                    remoteDeviceConnector.sendMessageToActivity(messageToActivity);
                } catch (Exception e) {
                    Log.e(TAG, "sendMessageToCustomerScreen: " + e.getMessage());
                }
            }
        });
    }

    public void startCustomerFacingActivity(String activityAction, String payload) {
        backgroundHandler.post(() -> {
            if (remoteDeviceConnector != null) {
                try {
                    CustomActivityRequest customActivityRequest = new CustomActivityRequest(activityAction, payload);
                    remoteDeviceConnector.startCustomActivity(customActivityRequest, new CustomActivityListener() {
                        @Override
                        public void onMessageFromActivity(MessageFromActivity messageFromActivity) {

                        }

                        @Override
                        public void onCustomActivityResult(CustomActivityResponse customActivityResponse) {

                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "sendMessageToCustomerScreen: " + e.getMessage());
                }
            }
        });
    }

    public String getMerchantId() {
        return merchantId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundHandler.post(() -> {
            if (remoteDeviceConnector != null) {
                remoteDeviceConnector.disconnect();
                remoteDeviceConnector = null;
            }
        });
        backgroundThread.quitSafely();
        if (mOrderConnector != null) {
            mOrderConnector.addOnOrderChangedListener(mOrderUpdateListener);
            mOrderConnector.disconnect();
            mOrderConnector = null;
        }
        if (mDisplayConnector != null) {
            mDisplayConnector.dispose();
            mDisplayConnector = null;
        }
        Log.d(TAG, "OrderListenerService stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        PendingIntent serviceIntent = PendingIntent.getService(getApplicationContext(),
                1001, new Intent(getApplicationContext(), OrderListenerService.class),
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            AlarmManagerCompat.setExact(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000,
                    serviceIntent
            );
        }
    }
}
