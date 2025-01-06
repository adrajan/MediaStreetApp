package ads.mediastreet.ai.model

data class OrderRequest(
    val retailerId: String,
    val orderId: String,
    val price: Double,
    val products: List<String>
)

data class AdMetadata(
    val width: Int,
    val height: Int,
    val format: String,
    val aspectRatio: String
)

data class Ad(
    val adId: String,
    val adType: String,
    val url: String,
    val metadata: AdMetadata
)

data class OrderResponse(
    val orderId: String,
    val impressionId: String,
    val timestamp: Long,
    val ad: Ad
)