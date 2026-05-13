package com.abir.kotlinposapp.data.repository

import com.abir.kotlinposapp.data.local.dao.OrderDao
import com.abir.kotlinposapp.data.local.entity.OrderEntity
import com.abir.kotlinposapp.data.local.entity.OrderItemEntity
import com.abir.kotlinposapp.data.local.entity.OrderWithItems
import com.abir.kotlinposapp.domain.model.Order
import com.abir.kotlinposapp.domain.model.OrderItem
import com.abir.kotlinposapp.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val dao: OrderDao
) : OrderRepository {

    override suspend fun saveOrder(order: Order) {
        val entity = OrderEntity(totalAmount = order.totalAmount, timestamp = order.timestamp)
        val itemEntities = order.items.map { it.toEntity() }
        dao.insertFullOrder(entity, itemEntities)
    }

    override fun getAllOrders(): Flow<List<Order>> =
        dao.getOrdersWithItems().map { list -> list.map { it.toDomain() } }
}

private fun OrderWithItems.toDomain() = Order(
    id = order.id,
    totalAmount = order.totalAmount,
    timestamp = order.timestamp,
    items = items.map { it.toDomain() }
)

private fun OrderItemEntity.toDomain() = OrderItem(
    productId = productId,
    productName = productName,
    productPrice = productPrice,
    quantity = quantity
)

private fun OrderItem.toEntity() = OrderItemEntity(
    orderId = 0, // assigned inside insertFullOrder
    productId = productId,
    productName = productName,
    productPrice = productPrice,
    quantity = quantity
)
