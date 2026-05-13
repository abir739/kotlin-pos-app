package com.abir.kotlinposapp.domain.usecase

import com.abir.kotlinposapp.domain.model.Order
import com.abir.kotlinposapp.domain.repository.OrderRepository
import javax.inject.Inject

class SaveOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(order: Order) = repository.saveOrder(order)
}
