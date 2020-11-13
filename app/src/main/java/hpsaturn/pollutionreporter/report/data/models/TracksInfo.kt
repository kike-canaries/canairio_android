package hpsaturn.pollutionreporter.report.data.models

import java.time.LocalDate

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
    val date: LocalDate,
    val deviceId: String,
    val lastLat: Double,
    val lastLon: Double,
    val lastTrackData: TracksData,
    val name: String,
    val size: Int
)
