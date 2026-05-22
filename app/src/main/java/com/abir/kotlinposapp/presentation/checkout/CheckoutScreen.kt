package com.abir.kotlinposapp.presentation.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abir.kotlinposapp.domain.model.CartItem
import com.abir.kotlinposapp.domain.model.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    innerPadding: PaddingValues,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showProductPicker by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.orderPlaced) {
        if (uiState.orderPlaced) {
            snackbarHostState.showSnackbar("Order placed successfully!")
            viewModel.onOrderPlacedHandled()
        }
    }

    LaunchedEffect(uiState.barcodeError) {
        uiState.barcodeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onBarcodeErrorHandled()
        }
    }

    // Product found online — ask user to confirm name + set price
    if (uiState.lookupState is BarcodeLookupState.Found) {
        ProductFoundDialog(
            found = uiState.lookupState as BarcodeLookupState.Found,
            onConfirm = { name, price ->
                viewModel.confirmAddOnlineProduct(
                    name = name,
                    barcode = (uiState.lookupState as BarcodeLookupState.Found).barcode,
                    price = price
                )
            },
            onDismiss = { viewModel.dismissLookupResult() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Checkout") },
                    actions = {
                        IconButton(onClick = { showScanner = true }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan barcode")
                        }
                        IconButton(onClick = { showProductPicker = true }) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = "Add product")
                        }
                    }
                )
                // Thin progress bar shown while looking up a barcode online.
                // Using LinearProgressIndicator (not Dialog) avoids creating a new window,
                // which would trigger Activity focus changes and camera flashing.
                if (uiState.lookupState is BarcodeLookupState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (uiState.cartItems.isEmpty()) {
            EmptyCartMessage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cartItems, key = { it.product.id }) { cartItem ->
                        CartItemCard(
                            cartItem = cartItem,
                            onIncrease = { viewModel.increaseQuantity(cartItem.product) },
                            onDecrease = { viewModel.decreaseQuantity(cartItem.product) },
                            onRemove = { viewModel.removeFromCart(cartItem.product) }
                        )
                    }
                }

                OrderSummary(
                    total = uiState.total,
                    onPlaceOrder = { viewModel.placeOrder() }
                )
            }
        }

    } // end Scaffold

    // Scanner overlay — fullscreen, on top of the Scaffold
    if (showScanner) {
        BarcodeScannerScreen(
            onBarcodeDetected = { barcode ->
                showScanner = false
                viewModel.addToCartByBarcode(barcode)
            },
            onClose = { showScanner = false }
        )
    }
    } // end Box

    if (showProductPicker) {
        ModalBottomSheet(
            onDismissRequest = { showProductPicker = false },
            sheetState = sheetState
        ) {
            ProductPickerSheet(
                products = products,
                onProductSelected = { product ->
                    viewModel.addToCart(product)
                    scope.launch {
                        sheetState.hide()
                        showProductPicker = false
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyCartMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Cart is empty", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap + to add products or scan a barcode",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.product.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "৳ %.2f each".format(cartItem.product.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Subtotal: ৳ %.2f".format(cartItem.subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDecrease) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
                cartItem.quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onIncrease) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun OrderSummary(total: Double, onPlaceOrder: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", style = MaterialTheme.typography.titleLarge)
            Text(
                "৳ %.2f".format(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onPlaceOrder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Place Order")
        }
    }
}

@Composable
private fun ProductPickerSheet(
    products: List<Product>,
    onProductSelected: (Product) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            "Select a Product",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        HorizontalDivider()
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No products yet. Add some in the Products tab.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(products, key = { it.id }) { product ->
                    TextButton(
                        onClick = { onProductSelected(product) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(product.name)
                            Text("৳ %.2f".format(product.price))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFoundDialog(
    found: BarcodeLookupState.Found,
    onConfirm: (name: String, price: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(found.name) }
    var price by rememberSaveable { mutableStateOf("") }
    val priceError = price.toDoubleOrNull()?.let { it <= 0 } ?: true
    val isValid = name.isNotBlank() && !priceError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Product found online") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Found on Open Food Facts. Edit the name if needed and set the price.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError && price.isNotEmpty(),
                    supportingText = if (priceError && price.isNotEmpty()) {
                        { Text("Enter a valid price greater than 0") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), price.toDouble()) },
                enabled = isValid
            ) { Text("Add to products & cart") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}
