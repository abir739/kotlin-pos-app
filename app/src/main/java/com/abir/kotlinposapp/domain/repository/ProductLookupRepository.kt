package com.abir.kotlinposapp.domain.repository

import com.abir.kotlinposapp.domain.model.ProductLookupResult

interface ProductLookupRepository {
    suspend fun lookupByBarcode(barcode: String): ProductLookupResult?
}
