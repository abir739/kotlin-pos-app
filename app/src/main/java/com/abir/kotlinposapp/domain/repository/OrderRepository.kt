package com.abir.kotlinposapp.domain.repository

import com.abir.kotlinposapp.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun saveOrder(order: Order)
    fun getAllOrders(): Flow<List<Order>>
}
