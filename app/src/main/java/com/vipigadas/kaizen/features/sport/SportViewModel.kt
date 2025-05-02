package com.vipigadas.kaizen.features.sport

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vipigadas.kaizen.api.Sport
import com.vipigadas.kaizen.ui.features.main.model.UiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SportViewModel @Inject constructor(
    private val context: Context, // Injected context for accessing resources
    private val repository: SportRepository,
) : ViewModel() {

    private val uiRepository = SportUiRepository(context, isFavorite = {
        repository.isFavorite(it.id)
    }, isExpand = {
        repository.isExpanded(it.id)
    })

    // Original sections from the repository to be used for filtering
    private var originalSections: List<Sport> = emptyList()

    // UI state data class
    data class SportsListUiState(
        val isLoading: Boolean = false,
        val items: List<UiModel> = emptyList(),
        val error: String? = null,
        val isSearchActive: Boolean = false,
        val searchQuery: String = "",
        val selectedSports: Set<String> = emptySet()
    )

    // Private mutable state flow
    private val _uiState = MutableStateFlow(SportsListUiState(isLoading = true))

    // Public immutable state flow
    val uiState: StateFlow<SportsListUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SportsListUiState(isLoading = true)
    )

    init {
        loadSportsEvents()
    }

    private fun loadSportsEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getSportsSections().fold(
                    onSuccess = { sections ->
                        // Store the original sections for filtering
                        originalSections = sections

                        // Apply any active filters
                        if (_uiState.value.isSearchActive || _uiState.value.selectedSports.isNotEmpty()) {
                            applyFilters()
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                items = convertToUiModelList(sections),
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load sports events"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun toggleSportExpanded(sportId: String) {
        repository.toggleExpand(sportId)
        loadSportsEvents()
    }

    fun toggleEventFavorite(eventId: String) {
        repository.toggleFavorite(eventId)
        loadSportsEvents()
    }

    /**
     * Converts the data model sections to a flat list of UI models.
     * If a section is expanded, its events are included in the list.
     */
    private fun convertToUiModelList(sections: List<Sport>): List<UiModel> {
        val uiModels = mutableListOf<UiModel>()

        sections.forEach { section ->
            // Add the sport header
            val sport = uiRepository.transformUISport(section)
            uiModels.add(sport)

            when (sport.isExpanded) {
                true -> section.events.forEach { event ->
                    uiModels.add(uiRepository.transformUIEvent(section, event))
                }

                false -> Unit
            }
        }

        return uiModels
    }

    fun retry() {
        loadSportsEvents()
    }

    /**
     * Update the search query and apply filters
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    /**
     * Set the search active state
     */
    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
        if (!active) {
            clearSearch()
        }
    }

    /**
     * Clear the search query and apply filters
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", isSearchActive = false)
        loadSportsEvents() // Reload the original data
    }

    /**
     * Toggle sport filter selection
     */
    fun toggleSportFilter(sport: String) {
        val currentFilters = _uiState.value.selectedSports.toMutableSet()
        if (currentFilters.contains(sport)) {
            currentFilters.remove(sport)
        } else {
            currentFilters.add(sport)
        }

        _uiState.value = _uiState.value.copy(selectedSports = currentFilters)
        applyFilters()
    }

    /**
     * Apply search query and sport filters to the original sections
     */
    private fun applyFilters() {
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val selectedSports = _uiState.value.selectedSports

        val filteredSections = originalSections.map { section ->
            // Apply sport filter
            if (selectedSports.isNotEmpty() && !selectedSports.contains(section.name)) {
                // If sports are filtered and this sport is not selected, return with empty events
                section.copy(events = emptyList())
            } else {
                // Apply search filter to events
                val filteredEvents = if (searchQuery.isBlank()) {
                    section.events
                } else {
                    section.events.filter { event ->
                        event.name.lowercase().contains(searchQuery)
                    }
                }

                // Auto-expand sections with matching events for search
                val shouldExpand = searchQuery.isNotBlank() && filteredEvents.isNotEmpty()

                // Check if the section should be expanded based on search or previous state
                val isExpanded = shouldExpand ||
                        (searchQuery.isBlank() && repository.isExpanded(section.id))

                section.copy(events = filteredEvents)
            }
        }.filter { section ->
            // Remove sections with no events after filtering
            if (searchQuery.isBlank() && selectedSports.isEmpty()) {
                true // Keep all sections if no filters applied
            } else {
                section.events.isNotEmpty() // Otherwise keep only sections with events
            }
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            items = convertToUiModelList(filteredSections),
            error = null
        )
    }

    // Convenience functions for the UI to use

    /**
     * Search events based on the provided query
     */
    fun searchEvents(query: String) {
        _uiState.value = _uiState.value.copy(
            isSearchActive = true,
            searchQuery = query
        )
        applyFilters()
    }
}