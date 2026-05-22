package com.abir.kotlinposapp.domain.usecase

import com.abir.kotlinposapp.domain.repository.ProductLookupRepository
import javax.inject.Inject

class LookupBarcodeOnlineUseCase @Inject constructor(
    private val repository: ProductLookupRepository
) {
    suspend operator fun invoke(barcode: String) = repository.lookupByBarcode(barcode)
}
