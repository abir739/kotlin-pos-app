package com.abir.kotlinposapp.presentation

sealed class Screen(val route: String) {
    object Products : Screen("products")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
}
