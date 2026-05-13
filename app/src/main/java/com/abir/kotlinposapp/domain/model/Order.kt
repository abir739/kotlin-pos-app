package com.abir.kotlinposapp.domain.model

data class OrderItem(
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val quantity: Int
) {
    val subtotal: Double get() = productPrice * quantity
}

data class Order(
    val id: Long = 0,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val timestamp: Long
)
