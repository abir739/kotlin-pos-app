package com.abir.kotlinposapp.presentation.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abir.kotlinposapp.domain.model.Product
import com.abir.kotlinposapp.presentation.checkout.BarcodeScannerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    innerPadding: PaddingValues,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // Hoisted dialog form state — preserved across dialog open/close cycles (e.g. when scanning)
    var dialogName by rememberSaveable { mutableStateOf("") }
    var dialogPrice by rememberSaveable { mutableStateOf("") }
    var dialogBarcode by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TopAppBar(title = { Text("Products") })

            Box(modifier = Modifier.weight(1f)) {
                if (products.isEmpty()) {
                    EmptyProductsMessage(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp, bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onEdit = {
                                    editingProduct = product
                                    dialogName = product.name
                                    dialogPrice = product.price.toString()
                                    dialogBarcode = product.barcode
                                    showDialog = true
                                },
                                onDelete = { viewModel.delete(product) }
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        dialogName = ""
                        dialogPrice = ""
                        dialogBarcode = ""
                        showDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add product")
                }
            }
        }

        // Fullscreen scanner — shown on top of everything, dialog is dismissed first
        if (showBarcodeScanner) {
            BarcodeScannerScreen(
                onBarcodeDetected = { barcode ->
                    dialogBarcode = barcode
                    showBarcodeScanner = false
                    showDialog = true
                },
                onClose = {
                    showBarcodeScanner = false
                    showDialog = true
                }
            )
        }
    }

    if (showDialog) {
        ProductDialog(
            product = editingProduct,
            name = dialogName,
            onNameChange = { dialogName = it },
            price = dialogPrice,
            onPriceChange = { dialogPrice = it },
            barcode = dialogBarcode,
            onBarcodeChange = { dialogBarcode = it },
            onRequestBarcodeScan = {
                showDialog = false
                showBarcodeScanner = true
            },
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.save(
                    Product(
                        id = editingProduct?.id ?: 0L,
                        name = dialogName.trim(),
                        price = dialogPrice.toDouble(),
                        barcode = dialogBarcode.trim()
                    )
                )
                showDialog = false
            }
        )
    }
}

@Composable
private fun EmptyProductsMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "No products yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Tap the + button below to add your first product",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "৳ %.2f".format(product.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.barcode.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            product.barcode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun ProductDialog(
    product: Product?,
    name: String,
    onNameChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    onRequestBarcodeScan: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val nameError = name.isBlank()
    val priceError = price.toDoubleOrNull()?.let { it <= 0 } ?: true
    val isValid = !nameError && !priceError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    singleLine = true,
                    isError = nameError && name.isNotEmpty(),
                    supportingText = if (nameError && name.isNotEmpty()) {
                        { Text("Name cannot be empty") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("Price") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError && price.isNotEmpty(),
                    supportingText = if (priceError && price.isNotEmpty()) {
                        { Text("Enter a valid price greater than 0") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = onBarcodeChange,
                    label = { Text("Barcode (optional)") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onRequestBarcodeScan) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Scan barcode"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = isValid) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
