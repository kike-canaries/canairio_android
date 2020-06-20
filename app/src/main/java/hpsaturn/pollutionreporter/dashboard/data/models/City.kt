package hpsaturn.pollutionreporter.dashboard.data.models


/**
 * Information about the monitoring station.
 * @property name Name of the monitoring station.
 * @property geo Latitude/Longitude of the monitoring station.
 * @property url for the attribution link.
 * See more here: https://aqicn.org/json-api/doc/
 */

data class City(
    val geo: List<Double>,
    val name: String,
    val url: String
)