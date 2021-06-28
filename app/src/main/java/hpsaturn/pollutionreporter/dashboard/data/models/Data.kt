package hpsaturn.pollutionreporter.dashboard.data.models


/**
 * Data from the air quality station.
 * @property aqi Real-time air quality information.
 * @property attributions List of EPA Attribution for the station.
 * @property city Information about the monitoring station.
 * @property idx Unique ID for the city monitoring station.
 * @property time Measurement time information.
 * See more here: https://aqicn.org/json-api/doc/
 */

data class Data(
    val aqi: Int,
    val attributions: List<Attribution>,
    val city: City,
    val idx: Int,
    val time: Time
)