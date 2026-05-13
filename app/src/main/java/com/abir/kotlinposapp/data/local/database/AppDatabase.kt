package com.abir.kotlinposapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abir.kotlinposapp.data.local.dao.OrderDao
import com.abir.kotlinposapp.data.local.dao.ProductDao
import com.abir.kotlinposapp.data.local.entity.OrderEntity
import com.abir.kotlinposapp.data.local.entity.OrderItemEntity
import com.abir.kotlinposapp.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
}
