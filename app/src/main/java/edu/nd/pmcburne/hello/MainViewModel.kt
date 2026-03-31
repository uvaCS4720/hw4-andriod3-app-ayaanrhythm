package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroundsUIState(
    val activeTag: String = "All",
    val searchText: String = "",
    val allTags: List<String> = listOf("All"),
    val visibleLocations: List<GroundsLocationEntity> = emptyList(),
    val activeLocation: GroundsLocationEntity? = null,
    val isSyncing: Boolean = true,
    val syncError: String? = null,
    val darkModeEnabled: Boolean = false
)

private data class GroundsFilterState(
    val activeTag: String,
    val searchQuery: String,
    val activeLocationID: Int?,
    val darkModeEnabled: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val groundsRepo = GroundsLocationsRepo(
        groundsDao = GroundsMapDatabase.getGroundsDB(application).groundsLocationService(),
        uvaLocationApi = UvaApiService.buildService()
    )

    private val chosenTag = MutableStateFlow("All")
    private val searchQueryFlow = MutableStateFlow("")
    private val chosenLocationId = MutableStateFlow<Int?>(null)
    private val loadingState = MutableStateFlow(true)
    private val errorState = MutableStateFlow<String?>(null)
    private val darkModeFlow = MutableStateFlow(false)

    private val groundsFilterState = combine(
        chosenTag,
        searchQueryFlow,
        chosenLocationId,
        darkModeFlow
    ) { tag, query, selectedId, darkMode ->
        GroundsFilterState(
            activeTag = tag,
            searchQuery = query,
            activeLocationID = selectedId,
            darkModeEnabled = darkMode
        )
    }

    val uiState: StateFlow<GroundsUIState> = combine(
        groundsRepo.uvaGroundsLocationsAll,
        groundsFilterState,
        loadingState,
        errorState
    ) { locations, filter, loading, error ->

        val tags = buildList {
            add("All")
            addAll(
                locations
                    .flatMap { it.locationTags }
                    .distinct()
                    .sorted()
            )
        }

        val trimmedQuery = filter.searchQuery.trim()

        val filteredLocations = locations.filter { location ->
            val matchesTag =
                filter.activeTag == "All" || filter.activeTag in location.locationTags

            val matchesQuery =
                trimmedQuery.isBlank() ||
                        location.locationName.contains(trimmedQuery, ignoreCase = true) ||
                        location.locationDetails.contains(trimmedQuery, ignoreCase = true) ||
                        location.locationTags.any { tag ->
                            tag.contains(trimmedQuery, ignoreCase = true)
                        }

            matchesTag && matchesQuery
        }

        GroundsUIState(
            activeTag = filter.activeTag,
            searchText = filter.searchQuery,
            allTags = tags,
            visibleLocations = filteredLocations,
            activeLocation = locations.firstOrNull { it.locationID == filter.activeLocationID },
            isSyncing = loading,
            syncError = error,
            darkModeEnabled = filter.darkModeEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroundsUIState()
    )

    init {
        syncLocations()
    }

    fun selectTag(tag: String) {
        chosenTag.value = tag
    }

    fun updateSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun locationChosen(location: GroundsLocationEntity) {
        chosenLocationId.value = location.locationID
    }

    fun closeLocationWindow() {
        chosenLocationId.value = null
    }

    fun darkModeToggle() {
        darkModeFlow.value = !darkModeFlow.value
    }

    private fun syncLocations() {
        viewModelScope.launch {
            loadingState.value = true
            errorState.value = null

            try {
                groundsRepo.groundLocationsSync()
            } catch (e: Exception) {
                errorState.value = e.message ?: "Failed to sync campus locations."
            } finally {
                loadingState.value = false
            }
        }
    }
}