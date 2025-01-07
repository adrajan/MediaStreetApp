package ads.mediastreet.ai.app

import ads.mediastreet.ai.model.AccountRequest
import ads.mediastreet.ai.model.AccountResponse
import ads.mediastreet.ai.model.HealthResponse
import ads.mediastreet.ai.model.ImpressionRequest
import ads.mediastreet.ai.model.ImpressionResponse
import ads.mediastreet.ai.model.OrderRequest
import ads.mediastreet.ai.model.OrderResponse
import ads.mediastreet.ai.model.RetailerStatsResponse
import ads.mediastreet.ai.utils.Variables
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {

    @POST(Variables.V1_AD + "ads/order")
    fun createOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>

    @POST(Variables.V1_AD +"ads/impressions")
    fun recordImpression(@Body impressionRequest: ImpressionRequest): Call<ImpressionResponse>

    @GET(Variables.V1_AD + "retailers/{retailerId}/stats")
    fun getRetailerStats(@Path("retailerId") retailerId: String): Call<RetailerStatsResponse>

    @GET(Variables.V1_AD + "health")
    fun getHealth(): Call<HealthResponse>

    @POST(Variables.V1_AD + "account")
    fun getAccountStatus(@Body accountRequest: AccountRequest): Call<AccountResponse>
}