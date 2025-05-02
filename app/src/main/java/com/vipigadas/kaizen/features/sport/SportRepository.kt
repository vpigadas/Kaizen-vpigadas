package com.vipigadas.kaizen.features.sport

import com.vipigadas.kaizen.api.ErrorCodes
import com.vipigadas.kaizen.api.NetworkHandler
import com.vipigadas.kaizen.api.NetworkResult
import com.vipigadas.kaizen.api.Sport
import com.vipigadas.kaizen.api.SportsApiService
import com.vipigadas.kaizen.api.SportsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface SportRepository {
    suspend fun getSportsSections(): Result<List<Sport>>
    fun toggleFavorite(eventId: String): Result<Boolean>
    fun toggleExpand(sportId: String): Result<Boolean>
    fun isExpanded(sportId: String): Boolean
    fun isFavorite(eventId: String): Boolean
}

/**
 * Repository implementation for sports data
 */
@Singleton
class SportRepositoryImpl @Inject constructor(
    private val apiService: SportsApiService,
    private val networkHandler: NetworkHandler
) : SportRepository {

    // Keep track of original data to avoid repeated repository calls
    private var originalData: List<Sport> = emptyList()

    private val disableSportSection: MutableSet<String> = mutableSetOf<String>()
    private val favoriteEvents: MutableSet<String> = mutableSetOf<String>()

    /**
     * Simulates fetching sports sections with their events from a data source.
     * In a real app, this would interact with a REST API or local database.
     */
    override suspend fun getSportsSections(): Result<List<Sport>> = withContext(Dispatchers.IO) {
        // Check if network is available
        if (!networkHandler.isNetworkAvailable()) {
            NetworkResult.Error(
                code = ErrorCodes.NO_INTERNET,
                message = "No internet connection and no cached data available"
            )
        }

        // Try to fetch from network
        when (val networkResult = apiService.getSports()) {
            is NetworkResult.Success -> {
                originalData = networkResult.data.sports
                Result.success(originalData)
            }

            is NetworkResult.Error -> {
                networkResult

                Result.failure(networkResult.message.let { IOException(it) } ?: IOException())
            }

            is NetworkResult.Exception -> {
                networkResult
                Result.failure(
                    networkResult.exception ?: IOException("Failed to connect to the server")
                )
            }

            is NetworkResult.Loading -> Result.success(emptyList())
        }
    }

    /**
     * Toggles favorite status of an event
     * This is a simulation - in a real app, this would update a database or API
     */
    override fun toggleFavorite(eventId: String): Result<Boolean> =
        when (favoriteEvents.contains(eventId)) {
            true -> {
                favoriteEvents.remove(eventId)
                Result.success(false)
            }

            false -> {
                favoriteEvents.add(eventId)
                Result.success(true)
            }
        }

    /**
     * Toggles favorite status of an event
     * This is a simulation - in a real app, this would update a database or API
     */
    override fun toggleExpand(sportId: String): Result<Boolean> =
        when (disableSportSection.contains(sportId)) {
            true -> {
                disableSportSection.remove(sportId)
                Result.success(true)
            }

            false -> {
                disableSportSection.add(sportId)
                Result.success(false)
            }
        }

    override fun isExpanded(sportId: String): Boolean = !(disableSportSection.contains(sportId))
    override fun isFavorite(eventId: String): Boolean = favoriteEvents.contains(eventId)
}