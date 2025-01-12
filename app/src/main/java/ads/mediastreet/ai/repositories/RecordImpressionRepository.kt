package ads.mediastreet.ai.repositories

import ads.mediastreet.ai.app.ApiClient
import ads.mediastreet.ai.model.ImpressionRequest
import ads.mediastreet.ai.model.ImpressionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RecordImpressionRepository {
    @JvmStatic
    inline fun recordImpression(
        orderId: String,
        impressionId: String,
        timestamp: Long,
        deviceId: String,
        retailerId: String,
        crossinline impressionResponse: ImpressionResponse?.() -> Unit
    ) {
        val impressionRequest = ImpressionRequest(orderId, impressionId, timestamp, deviceId, retailerId)
        ApiClient.getmInstance().api.recordImpression(impressionRequest)
            .enqueue(object : Callback<ImpressionResponse> {
                override fun onResponse(
                    call: Call<ImpressionResponse>, response: Response<ImpressionResponse>
                ) {
                    impressionResponse(response.body())
                }

                override fun onFailure(call: Call<ImpressionResponse>, t: Throwable) {
                    t.printStackTrace()
                    impressionResponse(null)
                }
            })
    }
}
