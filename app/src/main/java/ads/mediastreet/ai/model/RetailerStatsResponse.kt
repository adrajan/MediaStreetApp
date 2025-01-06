package ads.mediastreet.ai.model

data class RetailerStatsResponse(
    val title: String,
    val summary: RetailerStatsSummary,
    val monthly: List<RetailerMonthlyStats>
)

data class RetailerStatsSummary(
    val totalAds: Int,
    val totalOrders: Int,
    val paid: Double
)

data class RetailerMonthlyStats(
    val month: String,
    val ads: Int,
    val orders: Int,
    val paid: Double
)
