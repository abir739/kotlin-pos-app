package com.abir.kotlinposapp.data.remote.api

import com.abir.kotlinposapp.data.remote.dto.UpcItemDbResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UpcItemDbApi {
    @GET("lookup")
    suspend fun lookup(@Query("upc") upc: String): UpcItemDbResponseDto
}
