package hpsaturn.pollutionreporter.reports.open.data.models

/**
 * Information of single data point of a report.
 * @property altitude altitude of sea level of the data point.
 * @property p10 value of the P10 contaminant.
 * @property p25 value of the P2.5 contaminant.
 * @property spd value of SPD.
 * @property latitude Latitude of the data point.
 * @property longitude Longitude of the data point.
 * @property timestamp Timestamp of the data point.
 */

class TracksData(
    val id: String,
    val altitude: Double,
    val p10: Double,
    val p25: Double,
    val spd: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)