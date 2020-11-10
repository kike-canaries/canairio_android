package hpsaturn.pollutionreporter.dashboard.domain.usecases

import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository
import javax.inject.Inject

class FindNearestAirQualityStatus @Inject constructor(
    private val airQualityStatusRepository: AirQualityStatusRepository
) {

    suspend operator fun invoke(latitude: Double, longitude: Double) =
        airQualityStatusRepository.getNearestAirQualityStatus(latitude, longitude)
}