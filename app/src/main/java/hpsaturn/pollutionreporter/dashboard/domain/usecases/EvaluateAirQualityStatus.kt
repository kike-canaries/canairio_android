package hpsaturn.pollutionreporter.dashboard.domain.usecases

import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityScale
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import javax.inject.Inject


class EvaluateAirQualityStatus @Inject constructor() {
    operator fun invoke(airQualityStatus: AirQualityStatus): AirQualityScale =
        when (airQualityStatus.airQualityIndex) {
            in Int.MIN_VALUE..-1 -> throw IllegalArgumentException("No negative values for AQI.")
            in 0..50 -> AirQualityScale.GOOD
            in 51..100 -> AirQualityScale.MODERATE
            in 101..150 -> AirQualityScale.UNHEALTHY_FOR_SENSITIVE_GROUPS
            in 151..200 -> AirQualityScale.UNHEALTHY
            in 201..300 -> AirQualityScale.VERY_UNHEALTHY
            else -> AirQualityScale.HAZARDOUS
        }
}
