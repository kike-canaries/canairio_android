package hpsaturn.pollutionreporter.dashboard.domain.usecases

import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository

class FindNearestAirQualityStatus(private val airQualityStatusRepository: AirQualityStatusRepository) {

    suspend operator fun invoke(latitude: Double, longitude: Double) =
        airQualityStatusRepository.getNearestAirQualityStatus(latitude, longitude)
}