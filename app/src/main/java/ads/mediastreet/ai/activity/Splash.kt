package ads.mediastreet.ai.activity

import ads.mediastreet.ai.R
import ads.mediastreet.ai.databinding.SplashScreenBinding
import ads.mediastreet.ai.utils.SharedPreferencesHelper
import ads.mediastreet.ai.utils.Variables.ISFIRSTLAUNCH
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : Activity() {

    private lateinit var binding: SplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        val isFirstLaunch = SharedPreferencesHelper.getBoolean(ISFIRSTLAUNCH, true)
        if (isFirstLaunch) {
            SharedPreferencesHelper.saveBoolean(ISFIRSTLAUNCH, false)
            setContentView(binding.getRoot())
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                startActivity(Intent(this@Splash, Main::class.java))
                overridePendingTransition(R.anim.stay, R.anim.fadeout)
                finish()
            }
        } else {
            startActivity(Intent(this, Main::class.java))
            overridePendingTransition(R.anim.stay, R.anim.fadeout)
            finish()
        }

    }
}