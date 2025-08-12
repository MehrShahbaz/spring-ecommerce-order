package ecommerce.infrastructure

import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.stripe.StripeResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class StripeClientTest {
    private val stripeClient =
        mock<StripeClient> {
            val stripeResponse =
                StripeResponse(
                    "test_intent_id",
                    100,
                    "succeeded",
                    1234,
                    "eur",
                )
            on { createCheckoutSession(any()) } doReturn stripeResponse
            on { confirmPayment("test_intent_id") } doReturn stripeResponse
        }

    @Test
    fun `test create payment intent`() {
        // given
        val paymentRequest =
            PaymentRequest(
                "100",
                "eur",
                "pm_card_visa",
            )

        // when
        val response = stripeClient.createCheckoutSession(paymentRequest)

        // then
        assertThat(response?.id).isEqualTo("test_intent_id")
    }

    @Test
    fun `test confirm payment intent`() {
        // when
        val confirm = stripeClient.confirmPayment("test_intent_id")

        // then
        assertThat(confirm?.status).isEqualTo("succeeded")
    }
}
