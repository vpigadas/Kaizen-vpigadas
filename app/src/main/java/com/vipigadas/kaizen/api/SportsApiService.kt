package com.vipigadas.kaizen.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API Service interface for sports data
 * Using interface for better testability with mocks
 */
interface SportsApiService {
    suspend fun getSports(): NetworkResult<SportsData>
    fun getSportsAsFlow(): Flow<NetworkResult<SportsData>>
}

/**
 * Implementation of the SportsApiService using Ktor client
 */
@Singleton
class SportsApiServiceImpl @Inject constructor(
    private val client: HttpClient
) : SportsApiService {

    companion object {
        private const val SPORTS_ENDPOINT = "/MockSports/sports.json"
    }

    /**
     * Fetches sports data and returns a wrapped NetworkResult
     * This handles exceptions and provides a clean API
     */
    override suspend fun getSports(): NetworkResult<SportsData> {
        return try {
            val response: HttpResponse = client.get(SPORTS_ENDPOINT)

            when (response.status) {
                HttpStatusCode.OK -> {
                    val data = parseSportsData(response.body<Array<Sport>>())
                    NetworkResult.Success(data)
                }
                else -> {
                    Timber.e("API error: ${response.status}")
                    NetworkResult.Error(
                        code = response.status.value,
                        message = "Error fetching sports data: ${response.status.description}"
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while fetching sports data")
            NetworkResult.Exception(e)
        }
    }

    /**
     * Returns a Flow for sports data to support reactive programming
     * This is useful for UI that needs to observe data changes
     */
    override fun getSportsAsFlow(): Flow<NetworkResult<SportsData>> = flow {
        emit(NetworkResult.Loading())
        emit(getSports())
    }
}
