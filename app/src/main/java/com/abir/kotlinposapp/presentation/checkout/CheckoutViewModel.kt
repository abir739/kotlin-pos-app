package com.abir.kotlinposapp.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abir.kotlinposapp.domain.model.CartItem
import com.abir.kotlinposapp.domain.model.Order
import com.abir.kotlinposapp.domain.model.OrderItem
import com.abir.kotlinposapp.domain.model.Product
import com.abir.kotlinposapp.domain.usecase.AddProductUseCase
import com.abir.kotlinposapp.domain.usecase.GetProductByBarcodeUseCase
import com.abir.kotlinposapp.domain.usecase.GetProductsUseCase
import com.abir.kotlinposapp.domain.usecase.LookupBarcodeOnlineUseCase
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

// Represents every possible state of an online barcode lookup
sealed class BarcodeLookupState {
    object Idle : BarcodeLookupState()
    object Loading : BarcodeLookupState()
    data class Found(val name: String, val barcode: String) : BarcodeLookupState()
    // Barcode detected but not in Open Food Facts — user can still add it manually
    data class NotFound(val barcode: String) : BarcodeLookupState()
}

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val orderPlaced: Boolean = false,
    val barcodeError: String? = null,
    val lookupState: BarcodeLookupState = BarcodeLookupState.Idle
) {
    val total: Double get() = cartItems.sumOf { it.subtotal }
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
    private val saveOrderUseCase: SaveOrderUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val lookupBarcodeOnlineUseCase: LookupBarcodeOnlineUseCase,
    private val addProductUseCase: AddProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

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
            // 1. Check local DB first — instant, no network needed
            val localProduct = getProductByBarcodeUseCase(barcode)
            if (localProduct != null) {
                addToCart(localProduct)
                return@launch
            }

            // 2. Not in local DB — try the Open Food Facts API
            _uiState.update { it.copy(lookupState = BarcodeLookupState.Loading) }
            val result = lookupBarcodeOnlineUseCase(barcode)

            if (result != null) {
                // Product found online — ask the user to set a price before adding
                _uiState.update {
                    it.copy(lookupState = BarcodeLookupState.Found(result.name, result.barcode))
                }
            } else {
                // Not in local DB or Open Food Facts — let user add it manually with barcode pre-filled
                _uiState.update {
                    it.copy(lookupState = BarcodeLookupState.NotFound(barcode))
                }
            }
        }
    }

    // Called when the user confirms adding an online-found product with a price
    fun confirmAddOnlineProduct(name: String, barcode: String, price: Double) {
        viewModelScope.launch {
            addProductUseCase(Product(name = name, price = price, barcode = barcode))
            // Fetch the saved product to get its auto-generated Room ID
            val saved = getProductByBarcodeUseCase(barcode)
            if (saved != null) addToCart(saved)
            _uiState.update { it.copy(lookupState = BarcodeLookupState.Idle) }
        }
    }

    fun dismissLookupResult() {
        _uiState.update { it.copy(lookupState = BarcodeLookupState.Idle) }
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
