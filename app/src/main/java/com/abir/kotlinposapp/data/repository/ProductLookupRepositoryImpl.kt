package com.abir.kotlinposapp.data.repository

import com.abir.kotlinposapp.data.remote.api.OpenFoodFactsApi
import com.abir.kotlinposapp.data.remote.api.UpcItemDbApi
import com.abir.kotlinposapp.domain.model.ProductLookupResult
import com.abir.kotlinposapp.domain.repository.ProductLookupRepository
import javax.inject.Inject
import javax.inject.Named

class ProductLookupRepositoryImpl @Inject constructor(
    @Named("openFoodFacts") private val foodApi: OpenFoodFactsApi,
    @Named("openProductsFacts") private val productsApi: OpenFoodFactsApi,
    private val upcItemDbApi: UpcItemDbApi
) : ProductLookupRepository {

    // Throws IOException / HttpException on network failure.
    // Returns null only when the barcode is genuinely absent from all databases.
    override suspend fun lookupByBarcode(barcode: String): ProductLookupResult? =
        lookupOpenFacts(foodApi, barcode)
            ?: lookupOpenFacts(productsApi, barcode)
            ?: lookupUpcItemDb(barcode)

    private suspend fun lookupOpenFacts(api: OpenFoodFactsApi, barcode: String): ProductLookupResult? {
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

    private suspend fun lookupUpcItemDb(barcode: String): ProductLookupResult? {
        val response = upcItemDbApi.lookup(barcode)
        if (response.code != "OK") return null

        val item = response.items?.firstOrNull() ?: return null
        val name = item.title?.takeIf { it.isNotBlank() } ?: return null
        val brand = item.brand?.takeIf { it.isNotBlank() }
        val displayName = if (brand != null) "$name — $brand" else name
        return ProductLookupResult(name = displayName, barcode = barcode)
    }
}
