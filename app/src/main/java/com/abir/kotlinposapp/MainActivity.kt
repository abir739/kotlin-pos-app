package com.abir.kotlinposapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abir.kotlinposapp.presentation.Screen
import com.abir.kotlinposapp.presentation.checkout.CheckoutScreen
import com.abir.kotlinposapp.presentation.orders.OrdersScreen
import com.abir.kotlinposapp.presentation.products.ProductsScreen
import com.abir.kotlinposapp.ui.theme.KotlinPOSAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinPOSAppTheme {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route

                val navItems = listOf(
                    Triple(Screen.Products, "Products", Icons.Default.Inventory2),
                    Triple(Screen.Checkout, "Checkout", Icons.Default.ShoppingCart),
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
                                    icon = { Icon(icon, contentDescription = label) },
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
                        composable(Screen.Products.route) { ProductsScreen(innerPadding) }
                        composable(Screen.Checkout.route) { CheckoutScreen(innerPadding) }
                        composable(Screen.Orders.route) { OrdersScreen(innerPadding) }
                    }
                }
            }
        }
    }
}
