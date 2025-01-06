package ads.mediastreet.ai.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {

    private const val PREFERENCES_FILE_NAME = "mediaStreet_ai"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences =
            context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    // Save methods
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    // Retrieve methods
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    // Remove a specific key
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // Clear all preferences
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}