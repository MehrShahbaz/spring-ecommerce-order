package ecommerce.infrastructure

import ecommerce.dto.payment.PaymentRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun `test create payment intent`() {
        // given
        val paymentRequest =
            PaymentRequest(
                100,
                "eur",
                "pm_card_visa",
            )

        // when
        val response = stripeClient.createCheckoutSession(paymentRequest)

        // than
        assertThat(response?.amount).isEqualTo(paymentRequest.amount)
        assertThat(response?.id).isNotEmpty
    }

    @Test
    fun `test confirm payment intent`() {
        // given
        val paymentRequest =
            PaymentRequest(
                100,
                "eur",
                "pm_card_visa",
            )

        // when
        val intentId = stripeClient.createCheckoutSession(paymentRequest)?.id ?: ""
        val confirm = stripeClient.confirmPayment(intentId)

        // than
        assertThat(confirm?.amount).isEqualTo(paymentRequest.amount)
    }
}
