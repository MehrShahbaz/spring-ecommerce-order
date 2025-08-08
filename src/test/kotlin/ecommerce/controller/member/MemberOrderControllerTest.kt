package ecommerce.controller.member

import ecommerce.dto.order.OrderResponse
import ecommerce.dto.user.UserRequestDTO
import ecommerce.repository.CartRepository
import ecommerce.repository.UserRepository
import ecommerce.service.MemberAuthService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberOrderControllerTest {
    private lateinit var token: String

    @Autowired
    private lateinit var memberAuthService: MemberAuthService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @BeforeEach
    fun beforeInit() {
        token = memberAuthService.signUp(UserRequestDTO("user", "user.test@test.com", "hello123")).token
    }

    @AfterEach
    fun afterInit() {
        cartRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun getAllOrders() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/order")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getList("orders", OrderResponse::class.java)).isNotNull
    }

    @Test
    fun getOrderById() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/order/-1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}
