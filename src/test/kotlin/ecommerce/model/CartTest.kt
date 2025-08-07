package ecommerce.model

import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CartTest {
    lateinit var cart: Cart
    lateinit var options: List<Option>

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartProductRepository: CartProductRepository

    @BeforeEach
    fun initBeforeEachTest() {
        val member = userRepository.save(User("user@test.com", "user1234", "user"))
        cart = cartRepository.save(Cart(member))
        options =
            productRepository.save(
                Product(
                    "product",
                    "http://localhost:8080/image/upload/product1.jpg",
                    mutableListOf(
                        Option(
                            "Option 1",
                            15.0,
                            10,
                            "https://example.com/test.png",
                        ),
                    ),
                ),
            ).options
    }

    @AfterEach
    fun afterEachTest() {
        cartProductRepository.deleteAll()
        productRepository.deleteAll()
        cartRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun addProduct() {
        cart.addProduct(options[0])
        assertThat(cart.items.first().option.name).isEqualTo(options[0].name)
    }

    @Test
    fun `addProduct same option`() {
        cart.addProduct(options[0], 2)
        assertThat(cart.items.first().quantity).isEqualTo(2)
    }

    @Test
    fun decrementProduct() {
        cart.addProduct(options[0], 2)
        cart.decrementProduct(options[0])
        assertThat(cart.items.size).isEqualTo(1)
    }

    @Test
    fun `decrementProduct removes product`() {
        cart.addProduct(options[0])
        cart.decrementProduct(options[0])
        assertThat(cart.items).isEmpty()
    }

    @Test
    fun `decrementProduct throws if option not in cart`() {
        assertThrows<EntityNotFoundException> {
            cart.decrementProduct(options[0])
        }
    }

    @Test
    fun clear() {
        cart.addProduct(options[0], 5)
        cart.clear()
        assertThat(cart.items).isEmpty()
    }
}
