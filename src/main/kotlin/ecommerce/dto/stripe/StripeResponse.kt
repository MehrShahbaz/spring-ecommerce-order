package ecommerce.dto.stripe

class StripeResponse(
    val id: String,
    val amount: Int,
    val client_secret: String,
    val created: Int,
    val currency: String,
)