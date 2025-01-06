package ads.mediastreet.ai.repositories


import ads.mediastreet.ai.app.ApiClient
import ads.mediastreet.ai.model.OrderRequest
import ads.mediastreet.ai.model.OrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CreateOrderRespository {
    @JvmStatic
    inline fun createOrder(
        retailerId: String,
        orderId: String,
        price: Double,
        products: List<String>,
        crossinline orderResponse: OrderResponse?.() -> Unit
    ) {

        val orderRequest = OrderRequest(retailerId, orderId, price, products)
        ApiClient.getmInstance().api.createOrder(orderRequest)
            .enqueue(object : Callback<OrderResponse> {
                override fun onResponse(
                    call: Call<OrderResponse>, response: Response<OrderResponse>
                ) {
                    orderResponse(response.body())
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    t.printStackTrace()
                    orderResponse(null)
                }

            })
    }
}