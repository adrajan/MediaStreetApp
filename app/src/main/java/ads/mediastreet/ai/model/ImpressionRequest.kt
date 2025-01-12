package ads.mediastreet.ai.model

data class ImpressionRequest(
    val orderId: String,
    val impressionId: String,
    val timestamp: Long,
    val deviceId: String,  // Now using actual device ID
    val retailerId: String
)
