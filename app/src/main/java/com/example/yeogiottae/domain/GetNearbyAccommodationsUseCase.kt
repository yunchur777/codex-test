package com.example.yeogiottae.domain

import com.example.yeogiottae.data.Accommodation
import com.example.yeogiottae.data.AccommodationRepository
import com.example.yeogiottae.location.LocationCoordinate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyAccommodationsUseCase @Inject constructor(
    private val repository: AccommodationRepository
) {
    operator fun invoke(location: LocationCoordinate): Flow<List<Accommodation>> {
        return repository.getNearbyAccommodations(location)
    }
}
