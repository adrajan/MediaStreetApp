package ads.mediastreet.ai.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.clover.cfp.activity.CFPConstants;
import com.clover.cfp.activity.CloverCFPActivity;
import com.clover.cfp.activity.helper.CloverCFPActivityHelper;
import com.clover.cfp.activity.helper.CloverCFPCommsHelper;
import com.google.gson.Gson;

import ads.mediastreet.ai.R;
import ads.mediastreet.ai.databinding.CfpBinding;
import ads.mediastreet.ai.model.OrderResponse;
import ads.mediastreet.ai.repositories.RecordImpressionRepository;
import ads.mediastreet.ai.service.OrderListenerService;
import ads.mediastreet.ai.utils.DeviceUtils;
import kotlin.Unit;

public class CFP extends CloverCFPActivity {
    private CfpBinding binding;
    CloverCFPActivityHelper activityHelper;
    CloverCFPCommsHelper commsHelper;
    AppCompatTextView partnerLabel;
    private static final String TAG = "CFP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CfpBinding.inflate(getLayoutInflater());
        this.getIntent().putExtra("android.activity.launch_display", 1);
        setContentView(binding.getRoot());
        String deviceId = DeviceUtils.INSTANCE.getDeviceId(this);

        activityHelper = new CloverCFPActivityHelper(this);
        commsHelper = new CloverCFPCommsHelper(this, getIntent(), this);
        initViews();
        getData();
    }

    public void initViews() {
        binding.adinfotext.setText(Html.fromHtml(getString(R.string.partnerad)));
    }

    public void getData() {
        if (getIntent().hasExtra(CFPConstants.EXTRA_PAYLOAD)) {
            try {
                OrderResponse orderResponse = new Gson().fromJson(getIntent().getStringExtra(CFPConstants.EXTRA_PAYLOAD), OrderResponse.class);
                if (orderResponse != null && orderResponse.getAd() != null) {
                    Glide.with(this).load(orderResponse.getAd().getUrl())
                            .placeholder(new ColorDrawable(0xffdcdcdc))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(binding.adImageClover);

                    // Call the API to record impression
                    String orderId = orderResponse.getOrderId();
                    String impressionId = orderResponse.getImpressionId();
                    long timestamp = System.currentTimeMillis() / 1000L;
                    String deviceId = DeviceUtils.INSTANCE.getDeviceId(this); 
                    String merchantId = OrderListenerService.getMerchantId();

                    RecordImpressionRepository.recordImpression(orderId, impressionId, timestamp, deviceId, merchantId, impressionResponse -> {
                        if (impressionResponse != null) {
                            Log.d(TAG, "Impression recorded: " + impressionResponse.getMessage());
                        } else {
                            Log.e(TAG, "Failed to record impression");
                        }
                        return Unit.INSTANCE;
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(String payload) {
        Log.e(TAG, "onMessage: " + payload);
        if ("FINISH".equalsIgnoreCase(payload)) finishWithPayLoad("");
        super.onMessage(payload);
    }

    public void finishWithPayLoad(String resultPayload) {
        activityHelper.setResultAndFinish(RESULT_OK, resultPayload);
    }

    @Override
    protected void onDestroy() {
        activityHelper.dispose();
        commsHelper.dispose();
        super.onDestroy();
    }
}
