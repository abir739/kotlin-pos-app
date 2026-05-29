package com.abir.kotlinposapp.data.repository

import com.abir.kotlinposapp.data.remote.api.OpenFoodFactsApi
import com.abir.kotlinposapp.domain.model.ProductLookupResult
import com.abir.kotlinposapp.domain.repository.ProductLookupRepository
import javax.inject.Inject

class ProductLookupRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi
) : ProductLookupRepository {

    // Throws IOException / HttpException on network failure.
    // Returns null only when the barcode genuinely has no entry in Open Food Facts.
    override suspend fun lookupByBarcode(barcode: String): ProductLookupResult? {
        val response = api.getProduct(barcode)
        if (response.status != 1 || response.product == null) return null

        val product = response.product
        val name = product.productNameEn?.takeIf { it.isNotBlank() }
            ?: product.productName?.takeIf { it.isNotBlank() }
            ?: return null

        val brands = product.brands?.takeIf { it.isNotBlank() }
        val displayName = if (brands != null) "$name — $brands" else name

        return ProductLookupResult(name = displayName, barcode = barcode)
    }
}
