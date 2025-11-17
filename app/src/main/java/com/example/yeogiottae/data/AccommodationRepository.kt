package com.example.yeogiottae.data

import com.example.yeogiottae.location.LocationCoordinate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

interface AccommodationRepository {
    fun getNearbyAccommodations(location: LocationCoordinate): Flow<List<Accommodation>>
    suspend fun book(accommodation: Accommodation)
}

class FakeAccommodationRepository : AccommodationRepository {

    override fun getNearbyAccommodations(location: LocationCoordinate): Flow<List<Accommodation>> = flow {
        delay(500) // simulate network delay
        val seed = location.latitude.times(1000).toInt()
        val random = Random(seed)
        val items = (1..10).map { index ->
            val distance = random.nextDouble(100.0, 2500.0)
            Accommodation(
                id = "acc-$index",
                name = "${randomCityName(random)} 힐링 스테이 ${index}호",
                address = "${randomCityName(random)} ${index * 3}길",
                distanceMeters = distance,
                pricePerNight = random.nextInt(60000, 240000),
                thumbnailUrl = null
            )
        }.sortedBy { it.distanceMeters }
        emit(items)
    }

    override suspend fun book(accommodation: Accommodation) {
        delay(300)
    }

    private fun randomCityName(random: Random): String {
        val cities = listOf("강남", "홍대", "제주", "부산", "광안리", "여의도", "동대문")
        return cities[random.nextInt(cities.size)]
    }
}
