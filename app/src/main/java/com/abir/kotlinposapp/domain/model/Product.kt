package com.abir.kotlinposapp.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val barcode: String
)
