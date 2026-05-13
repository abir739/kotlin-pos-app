package com.abir.kotlinposapp.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}
