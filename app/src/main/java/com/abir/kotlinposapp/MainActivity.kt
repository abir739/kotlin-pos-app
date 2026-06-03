package com.abir.kotlinposapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abir.kotlinposapp.presentation.Screen
import com.abir.kotlinposapp.presentation.checkout.CheckoutScreen
import com.abir.kotlinposapp.presentation.checkout.CheckoutViewModel
import com.abir.kotlinposapp.presentation.orders.OrdersScreen
import com.abir.kotlinposapp.presentation.products.ProductsScreen
import com.abir.kotlinposapp.ui.theme.KotlinPOSAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Activity-scoped ViewModel — shared with CheckoutScreen via hiltViewModel()
    private val checkoutViewModel: CheckoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            KotlinPOSAppTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route

                val uiState by checkoutViewModel.uiState.collectAsStateWithLifecycle()
                val cartCount = uiState.cartItems.sumOf { it.quantity }

                val navItems = listOf(
                    Triple(Screen.Products, "Products", Icons.Default.Inventory2),
                    Triple(Screen.Checkout, "Cart", Icons.Default.ShoppingCart),
                    Triple(Screen.Orders, "Orders", Icons.Default.Receipt)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            navItems.forEach { (screen, label, icon) ->
                                NavigationBarItem(
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Products.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        if (screen == Screen.Checkout && cartCount > 0) {
                                            BadgedBox(badge = {
                                                Badge { Text(cartCount.toString()) }
                                            }) {
                                                Icon(icon, contentDescription = label)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = label)
                                        }
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Products.route,
                        contentAlignment = androidx.compose.ui.Alignment.TopStart
                    ) {
                        composable(Screen.Products.route) {
                            ProductsScreen(
                                innerPadding = innerPadding,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                        composable(Screen.Checkout.route) {
                            CheckoutScreen(
                                innerPadding = innerPadding,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme },
                                viewModel = checkoutViewModel
                            )
                        }
                        composable(Screen.Orders.route) {
                            OrdersScreen(
                                innerPadding = innerPadding,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                }
            }
        }
    }
}
