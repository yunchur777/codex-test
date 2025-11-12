package com.example.yeogiottae.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeogiottae.data.Accommodation
import com.example.yeogiottae.domain.GetNearbyAccommodationsUseCase
import com.example.yeogiottae.location.LocationProvider
import com.example.yeogiottae.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccommodationViewModel @Inject constructor(
    private val getNearbyAccommodations: GetNearbyAccommodationsUseCase,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _state = MutableStateFlow(AccommodationUiState())
    val state: StateFlow<AccommodationUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            locationProvider.observeLocation().collect { result ->
                when (result) {
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
    }

    private fun loadAccommodations(coordinate: com.example.yeogiottae.location.LocationCoordinate) {
        viewModelScope.launch {
            getNearbyAccommodations(coordinate).collect { accommodations ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        accommodations = accommodations,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun bookAccommodation(accommodation: Accommodation) {
        viewModelScope.launch {
            _state.update { it.copy(isBooking = true) }
            // In a real repository this would trigger a booking network call.
            // We only simulate a successful booking here.
            _state.update { current ->
                current.copy(
                    isBooking = false,
                    lastBooked = accommodation
                )
            }
        }
    }
}

data class AccommodationUiState(
    val isLoading: Boolean = false,
    val isBooking: Boolean = false,
    val accommodations: List<Accommodation> = emptyList(),
    val errorMessage: String? = null,
    val lastBooked: Accommodation? = null
)
