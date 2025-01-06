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
        ApiClient.getmInstance().api.getRetailerStats(retailerId).enqueue(object : Callback<RetailerStatsResponse> {
            override fun onResponse(
                call: Call<RetailerStatsResponse>,
                response: Response<RetailerStatsResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Stats fetched successfully for retailer: $retailerId")
                    callback(response.body())
                } else {
                    Log.e(TAG, "Error fetching stats: ${response.code()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<RetailerStatsResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch stats: ${t.message}")
                callback(null)
            }
        })
    }
}
