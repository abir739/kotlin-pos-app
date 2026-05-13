package com.abir.kotlinposapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)
