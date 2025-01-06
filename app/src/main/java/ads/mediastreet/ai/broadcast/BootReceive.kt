package ads.mediastreet.ai.broadcast

import ads.mediastreet.ai.service.OrderListenerService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService

class BootReceive : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (ACTION_BOOT_COMPLETED == intent?.action) {
            var serviceIntent = Intent(
                context,
                OrderListenerService::class.java
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(context!!, serviceIntent)
                // Use startForegroundService() for Android 8.0+
            } else {
                context!!.startService(serviceIntent) // Use startService() for lower Android versions
            }
        }
    }

}