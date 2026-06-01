package com.abir.kotlinposapp.di

import com.abir.kotlinposapp.data.remote.api.OpenFoodFactsApi
import com.abir.kotlinposapp.data.remote.api.UpcItemDbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

    private fun buildRetrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Food products — 3M+ entries, no key required
    @Provides
    @Singleton
    @Named("openFoodFacts")
    fun provideOpenFoodFactsApi(client: OkHttpClient): OpenFoodFactsApi =
        buildRetrofit("https://world.openfoodfacts.org/", client)
            .create(OpenFoodFactsApi::class.java)

    // General / non-food products — same API structure, different database
    @Provides
    @Singleton
    @Named("openProductsFacts")
    fun provideOpenProductsFactsApi(client: OkHttpClient): OpenFoodFactsApi =
        buildRetrofit("https://world.openproductsfacts.org/", client)
            .create(OpenFoodFactsApi::class.java)

    // Electronics, books, retail — 100 requests/day on trial tier, no key required
    @Provides
    @Singleton
    fun provideUpcItemDbApi(client: OkHttpClient): UpcItemDbApi =
        buildRetrofit("https://api.upcitemdb.com/prod/trial/", client)
            .create(UpcItemDbApi::class.java)
}
