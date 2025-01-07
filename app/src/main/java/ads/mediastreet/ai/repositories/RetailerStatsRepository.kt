package ads.mediastreet.ai.repositories

import ads.mediastreet.ai.app.ApiClient
import ads.mediastreet.ai.model.RetailerStatsResponse
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RetailerStatsRepository {
    private const val TAG = "RetailerStatsRepository"

    fun getRetailerStats(
        retailerId: String,
        callback: (RetailerStatsResponse?) -> Unit
    ) {
        Log.d(TAG, "Making API call to fetch stats for retailer: $retailerId")
        ApiClient.getmInstance().api.getRetailerStats(retailerId).enqueue(object : Callback<RetailerStatsResponse> {
            override fun onResponse(
                call: Call<RetailerStatsResponse>,
                response: Response<RetailerStatsResponse>
            ) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    Log.d(TAG, "Stats fetched successfully for retailer: $retailerId, Response: ${stats != null}")
                    if (stats == null) {
                        Log.e(TAG, "Response successful but body is null")
                    }
                    callback(stats)
                } else {
                    Log.e(TAG, "Error fetching stats: ${response.code()}, Error body: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<RetailerStatsResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch stats", t)
                callback(null)
            }
        })
    }
}
