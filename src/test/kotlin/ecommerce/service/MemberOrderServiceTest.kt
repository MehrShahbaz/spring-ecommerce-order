package ecommerce.service

import ecommerce.model.Cart
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.AfterTest

@SpringBootTest
class MemberOrderServiceTest {
    lateinit var member: User
    lateinit var cart: Cart

    @Autowired
    private lateinit var cartProductRepository: CartProductRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var orderService: MemberOrderService

    @BeforeEach
    fun initBefore() {
        member =
            userRepository.save(
                User(
                    "order@test.com",
                    "123456789",
                    "test",
                ),
            )
        val product =
            productRepository.save(
                Product(
                    "orderName",
                    "http://localhost:8080/image/upload/product1.jpg",
                    mutableListOf(
                        Option(
                            "name",
                            10.1,
                            51,
                            "http://localhost:8080/image/upload/product1.jpg",
                        ),
                    ),
                ),
            )
//        cart =
//            cartRepository.save(
//                Cart(
//                    member,
//                    mutableListOf(
//                        cartProductRepository.save(
//                            CartProduct(
//                                product.options.first(),
//                                10,
//                            ),
//                        ),
//                    ),
//                ),
//            )
    }

    @AfterTest
    fun tearDown() {
        cartRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun name() {
//        orderService.createCheckoutCartIntent(member.id)
    }
}
