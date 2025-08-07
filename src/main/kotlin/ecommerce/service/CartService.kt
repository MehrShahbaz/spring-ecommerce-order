package ecommerce.service

import ecommerce.dto.cartProduct.CartProductDTO
import ecommerce.dto.cartProduct.CartProductResponse
import ecommerce.enums.CartAction
import ecommerce.model.Cart
import ecommerce.model.CartProduct
import ecommerce.model.CartStatistic
import ecommerce.model.Option
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.CartStatisticRepository
import ecommerce.repository.OptionRepository
import ecommerce.utils.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CartService(
    private val cartStatisticRepository: CartStatisticRepository,
    private val optionRepository: OptionRepository,
    private val cartRepository: CartRepository,
    private val cartProductRepository: CartProductRepository,
) {
    fun getCartProducts(member: User): CartProductResponse {
        val cart = getCart(member)
        return CartProductResponse(
            cart.items.map { it.toDTO() },
        )
    }

    fun addProductToCart(
        member: User,
        optionId: Long,
    ): Long {
        val cart = getCart(member)
        val option = getValidProductOption(optionId)
        val addedItem = cart.addProduct(option)
        cartProductRepository.save(addedItem)

        cartStatisticRepository.save(
            CartStatistic(
                member.id,
                member.email,
                member.name,
                option.id,
                option.name,
                option.price,
                CartAction.ADD,
            ),
        )

        return addedItem.id
    }

    fun removeProductFromCart(
        member: User,
        optionId: Long,
    ) {
        val cart = getCart(member)
        val option = getValidProductOption(optionId)

        cart.decrementProduct(option)

        cartStatisticRepository.save(
            CartStatistic(
                member.id,
                member.email,
                member.name,
                option.id,
                option.name,
                option.price,
                CartAction.DELETE,
            ),
        )
    }

    fun clearCart(member: User) {
        val cart = getCart(member)

        if (cart.items.isEmpty()) {
            throw EntityNotFoundException("No items found")
        }

        val stats =
            cart.items.map {
                CartStatistic(
                    member.id,
                    member.email,
                    member.name,
                    it.option.id,
                    it.option.name,
                    it.option.price,
                    CartAction.DELETE,
                )
            }

        cartStatisticRepository.saveAll(stats)
        cart.clear()
    }

    private fun getCart(member: User): Cart {
        return cartRepository.findByUserIdOrUserNull(member.id)
            ?: throw EntityNotFoundException("Cart not found")
    }

    private fun getValidProductOption(optionId: Long): Option {
        return optionRepository.findById(optionId)
            .orElseThrow { EntityNotFoundException("Product option not found") }
    }

    private fun CartProduct.toDTO(): CartProductDTO {
        return CartProductDTO(
            option.id,
            option.name,
            option.price,
            option.imageUrl,
            quantity,
        )
    }
}
