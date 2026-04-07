package com.example.yeogiottae.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeogiottae.data.Accommodation
import com.example.yeogiottae.data.AccommodationRepository
import com.example.yeogiottae.domain.GetNearbyAccommodationsUseCase
import com.example.yeogiottae.location.LocationCoordinate
import com.example.yeogiottae.location.LocationProvider
import com.example.yeogiottae.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccommodationViewModel @Inject constructor(
    private val getNearbyAccommodations: GetNearbyAccommodationsUseCase,
    private val locationProvider: LocationProvider,
    private val repository: AccommodationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccommodationUiState())
    val state: StateFlow<AccommodationUiState> = _state

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = locationProvider.observeLocation().first()) {
                is LocationResult.Success -> loadAccommodations(result.coordinate)
                LocationResult.PermissionDenied -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "위치 권한이 필요합니다."
                    )
                }
                LocationResult.Unknown -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "위치 정보를 가져올 수 없습니다. 다시 시도해 주세요."
                    )
                }
            }
        }
    }

    private suspend fun loadAccommodations(coordinate: LocationCoordinate) {
        _state.update { it.copy(currentLocation = coordinate) }
        val accommodations = getNearbyAccommodations(coordinate).first()
        _state.update {
            it.copy(
                isLoading = false,
                accommodations = accommodations,
                errorMessage = null
            )
        }
    }

    fun bookAccommodation(accommodation: Accommodation) {
        viewModelScope.launch {
            _state.update { it.copy(isBooking = true, errorMessage = null) }
            runCatching {
                repository.book(accommodation)
            }.onSuccess {
                _state.update { current ->
                    current.copy(
                        isBooking = false,
                        lastBooked = accommodation
                    )
                }
            }.onFailure {
                _state.update { current ->
                    current.copy(
                        isBooking = false,
                        errorMessage = "예약 처리 중 문제가 발생했습니다. 다시 시도해 주세요."
                    )
                }
            }
        }
    }
}

data class AccommodationUiState(
    val isLoading: Boolean = false,
    val isBooking: Boolean = false,
    val accommodations: List<Accommodation> = emptyList(),
    val currentLocation: LocationCoordinate? = null,
    val errorMessage: String? = null,
    val lastBooked: Accommodation? = null
)
