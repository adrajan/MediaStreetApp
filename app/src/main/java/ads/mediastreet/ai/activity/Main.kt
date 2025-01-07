package ads.mediastreet.ai.activity

import ads.mediastreet.ai.databinding.ActivityMainBinding
import ads.mediastreet.ai.databinding.WelcomeScreenBinding
import ads.mediastreet.ai.service.OrderListenerService
import android.accounts.Account
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.util.CloverAuth
import okhttp3.OkHttpClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Main : AppCompatActivity() {
    private lateinit var binding: WelcomeScreenBinding
//    private var account: Account? = null
    private val TAG = "Main"

    //    private val executorService = Executors.newSingleThreadExecutor()
//    private val mainHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set version text
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionText.text = "v${packageInfo.versionName}"
        createNotificationChannel()
        startForegroundService()
    }

    /*private fun initViews() {


        binding.connectCloverAccountButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this@Main)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, 101)
                } else {
//                    getData()
                    try {
                        val result = CloverAuth.authenticate(this, true, 5L, TimeUnit.SECONDS)
                        Log.e(TAG, "Token result : $result")
                        Log.e(TAG, "Token: ${result.authToken}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Token: ERROR : $e")
                    }
                }
            }
        }
    }*/

    private fun startForegroundService() {
        // Start the OrderListenerService as a foreground service
        val serviceIntent = Intent(
            this,
            OrderListenerService::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent) // Use startForegroundService() for Android 8.0+
        } else {
            startService(serviceIntent) // Use startService() for lower Android versions
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CHANNEL_ID",  // Same ID used in the service
                "Order Listener Channel",  // Channel name shown to users
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Listening for order events"

            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(channel)
        }
    }

    /* private fun getCloverAuth() {
         executorService.execute {
             try {
                 val context = applicationContext
                 val cloverAuth = CloverAuth.authenticate(context)
                 mainHandler.postDelayed({
                     if (cloverAuth?.authToken != null) {
                         Log.e(TAG, "Token: ${cloverAuth.authToken}")
                     } else {
                         Log.e(TAG, "Token: ERROR")
                     }
                 }, 1000)
             } catch (e: Exception) {
                 Log.e(TAG, "Error authenticating", e)
                 mainHandler.post {
                     Log.e(TAG, "Token: ERROR FAILED")
                 }
             }
         }
     }

     fun getData() {
         try {
             val merchantId = "45RGP8JFG0ES1"
             val authToken = "54b9ca4c-294d-d34b-88d8-c5bc20283903"
             val merchantUri = "/v3/merchants/$merchantId"
             val context = applicationContext
             val cloverAuth = CloverAuth.authenticate(this, true, 5L, TimeUnit.SECONDS)
             val url = cloverAuth.baseUrl + merchantUri
             var client = OkHttpClient()
             var request =
                 okhttp3.Request.Builder().url(url).addHeader("Authorization", "Bearer $authToken")
                     .build()

             var response = client.newCall(request).execute()
             if (!response.isSuccessful) Log.e(TAG, "getData: Error $response")
             Log.e(TAG, "getData: respone $response")

         } catch (e: Exception) {
             Log.e(TAG, "getData: exception ${e.printStackTrace()}")
         }


     }*/

}