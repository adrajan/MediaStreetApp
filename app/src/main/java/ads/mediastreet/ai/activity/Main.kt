package ads.mediastreet.ai.activity

import ads.mediastreet.ai.adapter.MonthlyStatsAdapter
import ads.mediastreet.ai.databinding.WelcomeScreenBinding
import ads.mediastreet.ai.repositories.RetailerStatsRepository
import ads.mediastreet.ai.service.OrderListenerService
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager

class Main : Activity() {
    private lateinit var binding: WelcomeScreenBinding
    private lateinit var monthlyStatsAdapter: MonthlyStatsAdapter
    private var orderListenerService: OrderListenerService? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is OrderListenerService.LocalBinder) {
                orderListenerService = service.getService()
                orderListenerService?.setOnMerchantIdReadyListener(object : OrderListenerService.OnMerchantIdReadyListener {
                    override fun onMerchantIdReady(merchantId: String) {
                        fetchRetailerStats(merchantId)
                    }
                })
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            orderListenerService = null
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set version name
        binding.tvVersion.text = "Version ${packageManager.getPackageInfo(packageName, 0).versionName}"
        
        // Initialize RecyclerView
        monthlyStatsAdapter = MonthlyStatsAdapter()
        binding.rvMonthlyStats.apply {
            layoutManager = LinearLayoutManager(this@Main)
            adapter = monthlyStatsAdapter
        }
        
        createNotificationChannel()
        startAndBindService()
    }
    
    private fun startAndBindService() {
        val intent = Intent(this, OrderListenerService::class.java)
        // Start the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        // Bind to the service
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }
    
    private fun fetchRetailerStats(merchantId: String) {
        RetailerStatsRepository.getRetailerStats(merchantId) { response ->
            response?.let { stats ->
                runOnUiThread {
                    // Update title
                    binding.tvStatsTitle.text = stats.title
                    
                    // Update summary stats
                    binding.tvTotalAds.text = "Total Ads: ${stats.summary.totalAds}"
                    binding.tvTotalOrders.text = "Total Orders: ${stats.summary.totalOrders}"
                    binding.tvTotalPaid.text = "Total Paid: $${stats.summary.paid}"
                    
                    // Update monthly stats
                    monthlyStatsAdapter.updateStats(stats.monthly)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MediaStreet Service"
            val descriptionText = "MediaStreet Service Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MediaStreet", name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
    
    companion object {
        private const val TAG = "Main"
    }
}