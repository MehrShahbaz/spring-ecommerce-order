package ecommerce.infrastructure

import ecommerce.dto.payment.PaymentRequest
import ecommerce.utils.exception.StripeException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun `test stripe payment`() {
        // given
        val paymentRequest =
            PaymentRequest(
                1,
                "eur",
                "pm_card_visa",
            )

        // when
        assertThrows<StripeException>
        { stripeClient.createCheckoutSession(paymentRequest) }
    }
}
