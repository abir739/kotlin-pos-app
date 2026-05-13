package com.abir.kotlinposapp.domain.usecase

import com.abir.kotlinposapp.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(barcode: String) = repository.getProductByBarcode(barcode)
}
