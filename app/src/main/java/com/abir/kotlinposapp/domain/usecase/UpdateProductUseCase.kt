package com.abir.kotlinposapp.domain.usecase

import com.abir.kotlinposapp.domain.model.Product
import com.abir.kotlinposapp.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product) = repository.updateProduct(product)
}
