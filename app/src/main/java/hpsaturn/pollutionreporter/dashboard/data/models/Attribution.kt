package hpsaturn.pollutionreporter.dashboard.data.models


/**
 * Attributions of the administrator of the air station.
 * @property logo Logo of the administrator of the station.
 * @property name Name of the administrator of the station.
 * @property name Url of the administrator of the station.
 * See more here: https://aqicn.org/json-api/doc/
 */
data class Attribution(
    val logo: String,
    val name: String,
    val url: String
)