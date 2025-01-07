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
import kotlinx.coroutines.*

class Main : AppCompatActivity() {
    private lateinit var binding: WelcomeScreenBinding
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private val handler = Handler(Looper.getMainLooper())
    private val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set version text
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionText.text = "v${packageInfo.versionName}"

        startStatusChecks()
    }

    private fun startStatusChecks() {
        // Start health check polling immediately
        handler.post(object : Runnable {
            override fun run() {
                checkHealthStatus()
                handler.postDelayed(this, 5 * 60 * 1000) // 5 minutes
            }
        })

        // Check account status
        val merchantId = OrderListenerService.getMerchantId()
        if (merchantId == null) {
            // Wait for merchant ID to be available
            handler.postDelayed({ startStatusChecks() }, 1000)
            return
        }

        AccountRepository.checkAccountStatus(merchantId) { status ->
            updateAccountStatus(status)
            if (status.lowercase() == "approved") {
                createNotificationChannel()
                startForegroundService()
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