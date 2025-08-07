package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class OrderProducts(
    @Column(name = "option_id")
    var optionId: Long,
    @Column(name = "product_id")
    var productId: Long,
    @Column(name = "option_name")
    var optionName: String,
    @Column(name = "price", nullable = false)
    var price: Double,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
