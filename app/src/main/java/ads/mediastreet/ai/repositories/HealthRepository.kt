package ads.mediastreet.ai.repositories

import ads.mediastreet.ai.app.ApiClient
import ads.mediastreet.ai.model.HealthResponse
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object HealthRepository {
    private const val TAG = "HealthRepository"

    fun checkHealth(callback: (Boolean) -> Unit) {
        Log.d(TAG, "Making API call to check health status")
        ApiClient.getmInstance().api.getHealth().enqueue(object : Callback<HealthResponse> {
            override fun onResponse(call: Call<HealthResponse>, response: Response<HealthResponse>) {
                if (response.isSuccessful) {
                    val healthResponse = response.body()
                    Log.d(TAG, "Health check successful: ${healthResponse?.status}")
                    callback(healthResponse?.status == "healthy")
                } else {
                    Log.e(TAG, "Health check failed: ${response.code()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<HealthResponse>, t: Throwable) {
                Log.e(TAG, "Health check failed", t)
                callback(false)
            }
        })
    }
}
