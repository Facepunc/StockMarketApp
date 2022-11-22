package com.dorohanproject.data.repository

import com.opencsv.CSVReader
import com.dorohanproject.data.csv.CSVParser
import com.dorohanproject.data.csv.CompanyListingsParser
import com.dorohanproject.data.local.StockDatabase
import com.dorohanproject.data.mapper.toCompanyInfo
import com.dorohanproject.data.mapper.toCompanyListing
import com.dorohanproject.data.mapper.toCompanyListingEntity
import com.dorohanproject.data.remote.StockApi
import com.dorohanproject.domain.model.CompanyInfo
import com.dorohanproject.domain.model.CompanyListing
import com.dorohanproject.domain.model.IntradayInfo
import com.dorohanproject.domain.repository.StockRepository
import com.dorohanproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>,
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListings = dao.searchCompanyListing(query)
            emit(
                Resource.Success(
                data = localListings.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingsParser.parse(response.byteStream())
            } catch(e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Неможливо завантажити дані"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Неможливо завантажити дані"))
                null
            }

            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                emit(
                    Resource.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)
        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Не вдалося завантажити інформацію за цей день"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Не вдалося завантажити інформацію за цей день"
            )
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val result = api.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())
        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Неможливо завантажити інформацію про компанію"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Неможливо завантажити інформацію про компанію"
            )
        }
    }
}