package com.abir.kotlinposapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.abir.kotlinposapp.data.local.entity.OrderEntity
import com.abir.kotlinposapp.data.local.entity.OrderItemEntity
import com.abir.kotlinposapp.data.local.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    suspend fun insertFullOrder(order: OrderEntity, items: List<OrderItemEntity>) {
        val orderId = insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        insertOrderItems(itemsWithOrderId)
    }

    @Transaction
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getOrdersWithItems(): Flow<List<OrderWithItems>>
}
