package ecommerce.infrastructure

import ecommerce.dto.payment.PaymentBody
import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.stripe.StripeResponse
import ecommerce.utils.exception.StripeException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class StripeClient(
    @Value("\${stripe.secret-key}")
    private val stripeKey: String,
    private val stripeRestClient: RestClient
) {
    fun createCheckoutSession(req: PaymentRequest): StripeResponse? {
        val body = PaymentBody(
            req.amount,
            req.currency,
            req.paymentMethod
        ).toString()

        return try {
            val response =
                stripeRestClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $stripeKey")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(StripeResponse::class.java)

            response.body
        } catch (e: RestClientException) {
            throw StripeException(e.message)
        }
    }

    fun confirmPayment(intentId: String): StripeResponse? {
        return try {
            val response =
                stripeRestClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents/$intentId/confirm")
                    .headers { headers ->
                        headers.setBearerAuth(stripeKey)
                        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
                    }
                    .retrieve()
                    .toEntity(StripeResponse::class.java)

            response.body
        } catch (e: RestClientException) {
            throw StripeException(e.message)
        }
    }
}
