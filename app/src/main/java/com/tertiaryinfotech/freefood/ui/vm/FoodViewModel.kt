package com.tertiaryinfotech.freefood.ui.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tertiaryinfotech.freefood.data.AppSettings
import com.tertiaryinfotech.freefood.data.FoodListingRepository
import com.tertiaryinfotech.freefood.data.ImageStorage
import com.tertiaryinfotech.freefood.data.SettingsState
import com.tertiaryinfotech.freefood.location.LocationManager
import com.tertiaryinfotech.freefood.location.LocationSearchService
import com.tertiaryinfotech.freefood.model.Coordinate
import com.tertiaryinfotech.freefood.model.FoodListing
import com.tertiaryinfotech.freefood.model.Recurrence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoodViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FoodListingRepository.from(app)
    private val settings = AppSettings(app)
    private val locationManager = LocationManager(app)
    private val searchService = LocationSearchService(app)

    val myCountry: String get() = settings.myCountry

    val searchText = MutableStateFlow("")
    val settingsState: StateFlow<SettingsState> =
        settings.state.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())
    val currentLocation: StateFlow<LocationManager.LatLng?> = locationManager.currentLocation

    private val allListings: StateFlow<List<FoodListing>> =
        repository.listings.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Active listings after applying the user's country + distance filters. */
    val visibleListings: StateFlow<List<FoodListing>> =
        combine(allListings, searchText, settingsState, currentLocation) { listings, query, s, loc ->
            val now = System.currentTimeMillis()
            searchFilter(listings, query, now).filter { listing ->
                if (s.isSubscribed(listing.id)) return@filter true
                if (s.onlyMyCountry && settings.myCountry.isNotEmpty() &&
                    listing.country.isNotEmpty() && listing.country != settings.myCountry
                ) return@filter false
                if (s.limitDistance && loc != null) {
                    val d = listing.distanceFrom(loc.latitude, loc.longitude)
                    if (d != null && d > s.maxDistanceKm * 1_000) return@filter false
                }
                true
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun searchFilter(listings: List<FoodListing>, query: String, now: Long): List<FoodListing> {
        val active = listings.filter { it.isActive(now) }
            .sortedBy { it.combinedStartDate(now) }
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return active
        return active.filter {
            it.locationName.contains(trimmed, true) ||
                it.title.contains(trimmed, true) ||
                it.details.contains(trimmed, true)
        }
    }

    fun nearestListing(): FoodListing? {
        val visible = visibleListings.value
        val loc = currentLocation.value ?: return visible.firstOrNull()
        return visible.minByOrNull {
            it.distanceFrom(loc.latitude, loc.longitude) ?: Double.MAX_VALUE
        }
    }

    fun listingById(id: String): FoodListing? = allListings.value.firstOrNull { it.id == id }

    // MARK: lifecycle

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            repository.purgeExpired()
        }
    }

    fun onAppear() {
        viewModelScope.launch { locationManager.requestLocation() }
    }

    fun refreshLocation() {
        viewModelScope.launch { locationManager.requestLocation() }
    }

    fun hasLocationPermission() = locationManager.hasPermission()

    // MARK: actions

    fun setSearchText(value: String) { searchText.value = value }

    fun addListing(
        title: String,
        details: String,
        locationName: String,
        coordinate: Coordinate,
        country: String,
        date: Long,
        startTime: Long,
        endTime: Long,
        recurrence: Recurrence,
        photoUris: List<Uri>,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val paths = withContext(Dispatchers.IO) {
                photoUris.take(3).mapNotNull { ImageStorage.copyToInternal(getApplication(), it) }
            }
            repository.add(
                FoodListing(
                    title = title.trim(),
                    details = details.trim(),
                    locationName = locationName,
                    coordinate = coordinate,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    photos = paths,
                    recurrence = recurrence,
                    country = country,
                )
            )
            onDone()
        }
    }

    fun delete(listing: FoodListing) {
        viewModelScope.launch { repository.delete(listing) }
    }

    fun like(listing: FoodListing) {
        viewModelScope.launch {
            repository.like(listing)
            settings.markLiked(listing.id)
        }
    }

    fun toggleSubscribe(id: String) {
        viewModelScope.launch { settings.toggleSubscribe(id) }
    }

    fun setOnlyMyCountry(value: Boolean) = viewModelScope.launch { settings.setOnlyMyCountry(value) }
    fun setLimitDistance(value: Boolean) = viewModelScope.launch { settings.setLimitDistance(value) }
    fun setMaxDistanceKm(value: Double) = viewModelScope.launch { settings.setMaxDistanceKm(value) }
    fun clearSubscriptions() = viewModelScope.launch { settings.clearSubscriptions() }

    // MARK: location search (Add Listing)

    val searchResults = MutableStateFlow<List<LocationSearchService.PlaceResult>>(emptyList())

    fun searchLocation(query: String) {
        viewModelScope.launch { searchResults.value = searchService.search(query) }
    }

    fun clearSearchResults() { searchResults.value = emptyList() }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                return FoodViewModel(app) as T
            }
        }
    }
}
