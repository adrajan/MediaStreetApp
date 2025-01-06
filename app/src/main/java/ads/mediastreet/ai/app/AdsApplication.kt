package ads.mediastreet.ai.app

import ads.mediastreet.ai.utils.SharedPreferencesHelper
import android.app.Application

class AdsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesHelper.init(this)

    }
}