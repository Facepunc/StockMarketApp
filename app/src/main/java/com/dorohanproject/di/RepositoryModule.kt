package com.dorohanproject.di

import com.dorohanproject.data.csv.CSVParser
import com.dorohanproject.data.csv.CompanyListingsParser
import com.dorohanproject.data.csv.IntradayInfoParser
import com.dorohanproject.data.repository.StockRepositoryImpl
import com.dorohanproject.domain.model.CompanyListing
import com.dorohanproject.domain.model.IntradayInfo
import com.dorohanproject.domain.repository.StockRepository
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
    abstract fun bindCompanyListingsParser(
        companyListingsParser: CompanyListingsParser
    ): CSVParser<CompanyListing>

    @Binds
    @Singleton
    abstract fun bindIntradayInfoParser(
        intradayInfoParser: IntradayInfoParser
    ): CSVParser<IntradayInfo>

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository
}