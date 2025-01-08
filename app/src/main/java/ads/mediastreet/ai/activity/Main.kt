package ads.mediastreet.ai.activity

import ads.mediastreet.ai.*
import ads.mediastreet.ai.databinding.WelcomeScreenBinding
import ads.mediastreet.ai.repositories.AccountRepository
import ads.mediastreet.ai.repositories.HealthRepository
import ads.mediastreet.ai.service.OrderListenerService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v1.ResultStatus
import com.clover.sdk.v1.merchant.Merchant
import com.clover.sdk.v1.merchant.MerchantConnector
import com.clover.sdk.v1.ServiceConnector
import kotlinx.coroutines.*

class Main : AppCompatActivity() {
    private lateinit var binding: WelcomeScreenBinding
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private val handler = Handler(Looper.getMainLooper())
    private val TAG = "Main"
    private lateinit var mMerchantConnector: MerchantConnector
    private var merchantId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set version text
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionText.text = "v${packageInfo.versionName}"

        // Set initial account status to not approved
        updateAccountStatus("not_approved")

        // Initialize merchant connector
        initializeMerchantConnector()
    }

    private fun initializeMerchantConnector() {
        mMerchantConnector = MerchantConnector(this, CloverAccount.getAccount(this), null)
        mMerchantConnector.getMerchant(object : ServiceConnector.Callback<Merchant> {
            override fun onServiceSuccess(result: Merchant?, status: ResultStatus?) {
                merchantId = result?.id
                Log.d(TAG, "Merchant ID: $merchantId")
                startStatusChecks()
                if (merchantId == null) {
                    Log.e(TAG, "Merchant ID is null")
                    updateAccountStatus("not_approved")
                }
            }

            override fun onServiceFailure(status: ResultStatus?) {
                Log.e(TAG, "Failed to get merchant information: $status")
                updateAccountStatus("not_approved")
            }

            override fun onServiceConnectionFailure() {
                Log.e(TAG, "Failed to connect to merchant connector")
                updateAccountStatus("not_approved")
            }
        })
    }

    private fun startStatusChecks() {
        // Start health check polling with 5 minute interval
        val healthCheckRunnable = object : Runnable {
            override fun run() {
                checkHealthStatus()
                handler.postDelayed(this, 5 * 60 * 1000) // 5 minutes
            }
        }
        handler.postDelayed(healthCheckRunnable, 5 * 60 * 1000) // Start after 5 minutes
        checkHealthStatus() // Do initial check immediately

        // Check account status
        merchantId?.let { id ->
            AccountRepository.checkAccountStatus(id) { status ->
                updateAccountStatus(status)
                if (status.lowercase() == "approved") {
                    createNotificationChannel()
                    // Start service with merchant ID only when account is approved
                    val serviceIntent = Intent(this@Main, OrderListenerService::class.java).apply {
                        putExtra("merchant_id", merchantId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                }
            }
        }
    }

    private fun checkHealthStatus() {
        HealthRepository.checkHealth { isHealthy ->
            updateHealthStatus(isHealthy)
        }
    }

    private fun updateHealthStatus(isHealthy: Boolean) {
        binding.mediastreetStatus.setBackgroundResource(
            if (isHealthy) R.drawable.status_indicator_green
            else R.drawable.status_indicator_red
        )
    }

    private fun updateAccountStatus(status: String) {
        binding.accountStatus.setBackgroundResource(
            when (status.lowercase()) {
                "approved" -> R.drawable.status_indicator_green
                "pending" -> R.drawable.status_indicator_yellow
                else -> R.drawable.status_indicator_red
            }
        )
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(this, OrderListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "MediaStreet Service Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MediaStreet", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}