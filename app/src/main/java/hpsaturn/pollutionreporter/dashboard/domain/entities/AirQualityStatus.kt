package hpsaturn.pollutionreporter.dashboard.domain.entities

/**
 * Air quality information.
 * @property airQualityIndex Real-time air quality information.
 * @property stationName Name of the monitoring station.
 * @property stationLongitude Longitude of the monitoring station.
 * @property stationLatitude Latitude of the monitoring station.
 */

data class AirQualityStatus(
    val airQualityIndex: Int,
    val stationName: String,
    val stationLatitude: Double,
    val stationLongitude: Double
)