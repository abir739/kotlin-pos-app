package com.abir.kotlinposapp.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abir.kotlinposapp.domain.model.CartItem
import com.abir.kotlinposapp.domain.model.Order
import com.abir.kotlinposapp.domain.model.OrderItem
import com.abir.kotlinposapp.domain.model.Product
import com.abir.kotlinposapp.domain.usecase.GetProductByBarcodeUseCase
import com.abir.kotlinposapp.domain.usecase.GetProductsUseCase
import com.abir.kotlinposapp.domain.usecase.SaveOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val orderPlaced: Boolean = false,
    val barcodeError: String? = null
) {
    val total: Double get() = cartItems.sumOf { it.subtotal }
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
    private val saveOrderUseCase: SaveOrderUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    // Exposed for the "add product" dialog
    val products = getProductsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addToCart(product: Product) {
        _uiState.update { state ->
            val current = state.cartItems.toMutableList()
            val index = current.indexOfFirst { it.product.id == product.id }
            if (index >= 0) {
                current[index] = current[index].copy(quantity = current[index].quantity + 1)
            } else {
                current.add(CartItem(product, 1))
            }
            state.copy(cartItems = current)
        }
    }

    fun addToCartByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = getProductByBarcodeUseCase(barcode)
            if (product != null) {
                addToCart(product)
            } else {
                _uiState.update { it.copy(barcodeError = "No product found for this barcode") }
            }
        }
    }

    fun onBarcodeErrorHandled() {
        _uiState.update { it.copy(barcodeError = null) }
    }

    fun increaseQuantity(product: Product) = addToCart(product)

    fun decreaseQuantity(product: Product) {
        _uiState.update { state ->
            val current = state.cartItems.toMutableList()
            val index = current.indexOfFirst { it.product.id == product.id }
            if (index >= 0) {
                val item = current[index]
                if (item.quantity > 1) current[index] = item.copy(quantity = item.quantity - 1)
                else current.removeAt(index)
            }
            state.copy(cartItems = current)
        }
    }

    fun removeFromCart(product: Product) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.filter { it.product.id != product.id })
        }
    }

    fun placeOrder() {
        val items = _uiState.value.cartItems
        if (items.isEmpty()) return
        viewModelScope.launch {
            val order = Order(
                items = items.map {
                    OrderItem(
                        productId = it.product.id,
                        productName = it.product.name,
                        productPrice = it.product.price,
                        quantity = it.quantity
                    )
                },
                totalAmount = _uiState.value.total,
                timestamp = System.currentTimeMillis()
            )
            saveOrderUseCase(order)
            _uiState.update { CheckoutUiState(orderPlaced = true) }
        }
    }

    fun onOrderPlacedHandled() {
        _uiState.update { it.copy(orderPlaced = false) }
    }
}
