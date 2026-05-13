package com.abir.kotlinposapp.domain.repository

import com.abir.kotlinposapp.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
}
