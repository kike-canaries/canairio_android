package hpsaturn.pollutionreporter.dashboard.data.models


/**
 * Measurement time information.
 * @property s Local measurement time time.
 * @property tz Station timezone.
 * See more here: https://aqicn.org/json-api/doc/
 */
data class Time(
    val s: String,
    val tz: String
)