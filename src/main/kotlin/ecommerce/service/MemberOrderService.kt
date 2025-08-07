package ecommerce.service

import ecommerce.dto.cartProduct.CartProductDTO
import ecommerce.dto.order.OrderIntentResponse
import ecommerce.dto.order.OrderProductResponse
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.payment.PaymentRequest
import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentOption
import ecommerce.infrastructure.StripeClient
import ecommerce.model.MemberOrder
import ecommerce.model.OrderProducts
import ecommerce.model.User
import ecommerce.repository.MemberOrderRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.EntityNotFoundException
import ecommerce.utils.exception.StripeException
import org.springframework.stereotype.Service

@Service
class MemberOrderService(
    val orderRepository: MemberOrderRepository,
    val optionRepository: OptionRepository,
    val cartService: CartService,
    val stripeClient: StripeClient,
    private val userRepository: UserRepository,
) {
    fun getUserOrders(userId: Long): List<OrderResponse> {
        return orderRepository.findAllByUserId(userId).map { it.toOrderResponse() }
    }

    fun getOrderById(orderId: Long): OrderResponse {
        return orderRepository.findById(orderId).orElseThrow {
            throw EntityNotFoundException("Order with id $orderId not found")
        }.toOrderResponse()
    }

    fun createCheckoutCartIntent(userId: Long): OrderIntentResponse {
        val member = getUser(userId)
        val cartProducts = getCartProducts(member)
        val paymentRequest = createPaymentRequest(cartProducts)
        val paymentIntentId = stripeClient.createCheckoutSession(paymentRequest)!!.id
        val order = createOrder(member, cartProducts, paymentIntentId)
        return OrderIntentResponse(paymentIntentId, order.id)
    }

    fun confirmCheckout(orderId: Long) {
        val order = getOrder(orderId)
        val member = getUser(order.userId)
        if (order.status == OrderStatus.REJECTED) {
            order.incrementAttempt()
        }
        try {
            stripeClient.confirmPayment(order.paymentId)
            cartService.clearCart(member)
            order.status = OrderStatus.COMPLETED
        } catch (e: StripeException) {
            order.status = OrderStatus.REJECTED
            throw StripeException(e.message)
        }
    }

    private fun createOrder(
        user: User,
        cartProducts: List<CartProductDTO>,
        paymentId: String,
    ): MemberOrder {
        return orderRepository.save(
            MemberOrder(
                cartProducts.map {
                    OrderProducts(
                        it.optionId,
                        0L,
                        it.name,
                        it.price,
                        it.quantity,
                    )
                },
                user.id,
                user.email,
                paymentId,
                PaymentOption.STRIPE,
                calculateTotal(cartProducts),
                OrderStatus.PENDING,
            ),
        )
    }

    private fun calculateTotal(cartProducts: List<CartProductDTO>): Double {
        return cartProducts.sumOf { it.price * it.quantity }
    }

    private fun createPaymentRequest(cartProducts: List<CartProductDTO>): PaymentRequest {
        return PaymentRequest(
            calculateTotal(cartProducts).toInt(),
        )
    }

    private fun getOrder(orderId: Long): MemberOrder {
        return orderRepository.findById(orderId).orElseThrow { throw EntityNotFoundException("Order with id $orderId not found") }
    }

    private fun getCartProducts(member: User): List<CartProductDTO> {
        return cartService.getCartProducts(member).products.takeIf { it.isNotEmpty() }
            ?: throw EntityNotFoundException("No products found")
    }

    private fun getUser(userId: Long): User {
        return userRepository.findById(userId).orElseThrow { throw EntityNotFoundException("User with id $userId not found") }
    }

    private fun MemberOrder.toOrderResponse(): OrderResponse {
        return OrderResponse(
            id,
            createdAt,
            status,
            totalAmount,
            paymentId,
            paymentOption,
            optionProducts.map { it.toResponse() },
        )
    }

    private fun OrderProducts.toResponse(): OrderProductResponse {
        return OrderProductResponse(
            price,
            quantity,
            optionRepository.findById(optionId).orElse(null)?.imageUrl,
        )
    }
}
