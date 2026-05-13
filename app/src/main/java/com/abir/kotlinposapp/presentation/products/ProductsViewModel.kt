package com.abir.kotlinposapp.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abir.kotlinposapp.domain.model.Product
import com.abir.kotlinposapp.domain.usecase.AddProductUseCase
import com.abir.kotlinposapp.domain.usecase.DeleteProductUseCase
import com.abir.kotlinposapp.domain.usecase.GetProductsUseCase
import com.abir.kotlinposapp.domain.usecase.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    val products = getProductsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun save(product: Product) {
        viewModelScope.launch {
            if (product.id == 0L) addProductUseCase(product)
            else updateProductUseCase(product)
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch { deleteProductUseCase(product) }
    }
}
