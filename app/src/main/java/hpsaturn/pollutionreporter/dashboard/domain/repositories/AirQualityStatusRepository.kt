package hpsaturn.pollutionreporter.dashboard.domain.repositories

import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus

interface AirQualityStatusRepository {
    suspend fun getNearestAirQualityStatus(latitude: Double, longitude: Double): AirQualityStatus
}