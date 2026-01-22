package page.ooooo.geoshare.lib.billing

import androidx.compose.runtime.Immutable

@Immutable
data class Offer(
    val token: String,
    val formattedPrice: String,
    val period: Period,
    val productId: String,
) {
    enum class Period {
        ONE_TIME,
        MONTHLY,
    }
}
