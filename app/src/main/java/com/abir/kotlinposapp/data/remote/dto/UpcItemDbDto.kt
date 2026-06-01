package com.abir.kotlinposapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpcItemDbResponseDto(
    @SerializedName("code") val code: String,
    @SerializedName("items") val items: List<UpcItemDto>?
)

data class UpcItemDto(
    @SerializedName("title") val title: String?,
    @SerializedName("brand") val brand: String?
)
