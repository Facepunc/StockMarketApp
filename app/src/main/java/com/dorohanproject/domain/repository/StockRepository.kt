package com.dorohanproject.domain.repository

import com.dorohanproject.domain.model.CompanyInfo
import com.dorohanproject.domain.model.CompanyListing
import com.dorohanproject.domain.model.IntradayInfo
import com.dorohanproject.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

    suspend fun getIntradayInfo(
        symbol: String
    ): Resource<List<IntradayInfo>>

    suspend fun getCompanyInfo(
        symbol: String
    ): Resource<CompanyInfo>
}