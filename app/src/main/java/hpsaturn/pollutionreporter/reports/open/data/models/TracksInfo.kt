package hpsaturn.pollutionreporter.reports.open.data.models

/**
 * Basic information of a sensor report.
 * @property deviceId ID of the device that gathered the data.
 * @property date Date of the report.
 * @property lastLat Latitude of the last data point.
 * @property lastLon Longitude of the last data point.
 * @property name Name of the device that gathered the data.
 * @property size Number of data points gathered.
 */

data class TracksInfo(
    val date: String = "",
    val deviceId: String = "",
    val lastLat: Double = 0.0,
    val lastLon: Double = 0.0,
    val lastTrackData: TracksData? = null,
    val name: String = "",
    val size: Int = 0
)
