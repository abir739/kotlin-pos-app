package com.abir.kotlinposapp.data.repository

import com.abir.kotlinposapp.data.remote.api.OpenFoodFactsApi
import com.abir.kotlinposapp.domain.model.ProductLookupResult
import com.abir.kotlinposapp.domain.repository.ProductLookupRepository
import javax.inject.Inject

class ProductLookupRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi
) : ProductLookupRepository {

    override suspend fun lookupByBarcode(barcode: String): ProductLookupResult? {
        return try {
            val response = api.getProduct(barcode)
            if (response.status != 1 || response.product == null) return null

            val product = response.product
            val name = product.productNameEn?.takeIf { it.isNotBlank() }
                ?: product.productName?.takeIf { it.isNotBlank() }
                ?: return null

            val brands = product.brands?.takeIf { it.isNotBlank() }
            val displayName = if (brands != null) "$name — $brands" else name

            ProductLookupResult(name = displayName, barcode = barcode)
        } catch (e: Exception) {
            null
        }
    }
}
