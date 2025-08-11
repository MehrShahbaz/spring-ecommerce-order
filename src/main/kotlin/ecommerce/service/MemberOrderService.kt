package ecommerce.service

import ecommerce.dto.cartProduct.CartProductDto
import ecommerce.dto.order.OrderIntentResponse
import ecommerce.dto.order.OrderProductResponse
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.stripe.StripeResponse
import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentOption
import ecommerce.infrastructure.StripeClient
import ecommerce.model.MemberOrder
import ecommerce.model.OrderProduct
import ecommerce.model.User
import ecommerce.repository.MemberOrderRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.EntityNotFoundException
import ecommerce.utils.exception.LowStockException
import ecommerce.utils.exception.StripeException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
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
        checkCartProducts(cartProducts)
        val paymentRequest = createPaymentRequest(cartProducts)
        val paymentIntentId = stripeClient.createCheckoutSession(paymentRequest)!!.id
        val order = createOrder(member, cartProducts, paymentIntentId)
        return OrderIntentResponse(paymentIntentId, order.id)
    }

    fun confirmCheckout(orderId: Long): StripeResponse? {
        val order = getOrder(orderId)
        val member = getUser(order.userId)
        val cartProducts = getCartProducts(member)
        checkCartProducts(cartProducts)
        if (order.status == OrderStatus.REJECTED) {
            order.incrementAttempt()
        }
        return try {
            val response = stripeClient.confirmPayment(order.paymentId)
            cartService.checkoutCart(member)
            order.status = OrderStatus.COMPLETED
            response
        } catch (e: StripeException) {
            order.status = OrderStatus.REJECTED
            throw StripeException(e.message)
        }
    }

    private fun checkCartProducts(cartProducts: List<CartProductDto>) {
        cartProducts.forEach {
            val option =
                optionRepository.findById(it.optionId).orElseThrow {
                    throw EntityNotFoundException("Option with id ${it.optionId} not found")
                }
            if (option.quantity < it.quantity) {
                throw LowStockException("Option with id ${it.optionId} is low on stock")
            }
        }
    }

    private fun createOrder(
        user: User,
        cartProductDto: List<CartProductDto>,
        paymentId: String,
    ): MemberOrder {
        return orderRepository.save(
            MemberOrder(
                cartProductDto.map {
                    OrderProduct(
                        it.optionId,
                        it.name,
                        it.price,
                        it.quantity,
                    )
                },
                user.id,
                user.email,
                paymentId,
                PaymentOption.STRIPE,
                calculateTotal(cartProductDto),
                OrderStatus.PENDING,
            ),
        )
    }

    private fun calculateTotal(cartProductDtos: List<CartProductDto>): Double {
        return cartProductDtos.sumOf { it.price * it.quantity }
    }

    private fun createPaymentRequest(cartProductDtos: List<CartProductDto>): PaymentRequest {
        return PaymentRequest(
            (calculateTotal(cartProductDtos) * 100).toInt().toString(),
        )
    }

    private fun getOrder(orderId: Long): MemberOrder {
        return orderRepository.findById(orderId).orElseThrow { throw EntityNotFoundException("Order with id $orderId not found") }
    }

    private fun getCartProducts(member: User): List<CartProductDto> {
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

    private fun OrderProduct.toResponse(): OrderProductResponse {
        return OrderProductResponse(
            price,
            quantity,
            optionRepository.findById(optionId).orElse(null)?.imageUrl,
        )
    }
}
