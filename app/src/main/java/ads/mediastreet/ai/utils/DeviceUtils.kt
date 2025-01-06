package ads.mediastreet.ai.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log

object DeviceUtils {
    private const val TAG = "DeviceUtils"

    fun getDeviceId(context: Context): String {
        return try {
            // On Clover devices, Build.SERIAL should contain the device serial number
            if (Build.SERIAL != null && Build.SERIAL != "unknown") {
                Log.d(TAG, "Using Clover device serial: ${Build.SERIAL}")
                Build.SERIAL
            } else {
                Log.d(TAG, "Falling back to Android ID")
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device serial, falling back to Android ID", e)
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}
