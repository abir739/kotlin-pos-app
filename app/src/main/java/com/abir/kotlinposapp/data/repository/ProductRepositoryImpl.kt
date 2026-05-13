package com.abir.kotlinposapp.data.repository

import com.abir.kotlinposapp.data.local.dao.ProductDao
import com.abir.kotlinposapp.data.local.entity.ProductEntity
import com.abir.kotlinposapp.domain.model.Product
import com.abir.kotlinposapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> =
        dao.getAllProducts().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProductByBarcode(barcode: String): Product? =
        dao.getProductByBarcode(barcode)?.toDomain()

    override suspend fun addProduct(product: Product) =
        dao.insertProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        dao.updateProduct(product.toEntity())

    override suspend fun deleteProduct(product: Product) =
        dao.deleteProduct(product.toEntity())
}

private fun ProductEntity.toDomain() = Product(id, name, price, barcode)
private fun Product.toEntity() = ProductEntity(id, name, price, barcode)
