package com.abir.kotlinposapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponseDto(
    @SerializedName("status") val status: Int,
    @SerializedName("product") val product: OpenFoodFactsProductDto?
)

data class OpenFoodFactsProductDto(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_name_en") val productNameEn: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("quantity") val quantity: String?
)
