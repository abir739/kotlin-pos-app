package com.abir.kotlinposapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
