package com.abir.kotlinposapp.domain.usecase

import com.abir.kotlinposapp.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke() = repository.getAllProducts()
}
