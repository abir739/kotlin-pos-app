package com.abir.kotlinposapp.data.remote.api

import com.abir.kotlinposapp.data.remote.dto.OpenFoodFactsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponseDto
}
