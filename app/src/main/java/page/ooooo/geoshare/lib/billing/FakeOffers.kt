package page.ooooo.geoshare.lib.billing

val FakeOneTimeOffer = Offer(
    token = "fake_one_time_offer",
    formattedPrice = "$19",
    period = Offer.Period.ONE_TIME,
    productId = "fake_one_time_product"
)

val FakeSubscriptionOffer = Offer(
    token = "fake_subscription_offer",
    formattedPrice = "$1.5",
    period = Offer.Period.MONTHLY,
    productId = "fake_subscription_product"
)
