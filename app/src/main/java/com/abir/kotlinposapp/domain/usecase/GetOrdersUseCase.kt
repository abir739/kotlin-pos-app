package com.abir.kotlinposapp.domain.usecase

import com.abir.kotlinposapp.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke() = repository.getAllOrders()
}
