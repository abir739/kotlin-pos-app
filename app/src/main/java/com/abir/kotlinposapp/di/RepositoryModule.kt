package com.abir.kotlinposapp.di

import com.abir.kotlinposapp.data.repository.OrderRepositoryImpl
import com.abir.kotlinposapp.data.repository.ProductRepositoryImpl
import com.abir.kotlinposapp.domain.repository.OrderRepository
import com.abir.kotlinposapp.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}
