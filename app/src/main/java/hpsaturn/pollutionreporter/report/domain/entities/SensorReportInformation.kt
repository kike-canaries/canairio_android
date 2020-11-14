package hpsaturn.pollutionreporter.report.domain.entities

import java.time.LocalDate

/**
 * Basic information of a sensor report.
 * @property deviceId ID of the device that gathered the data.
 * @property date Date of the report.
 * @property lastLatitude Latitude of the last data point.
 * @property lastLongitude Longitude of the last data point.
 * @property name Name of the device that gathered the data.
 * @property numberOfPoints Number of data points gathered.
 */

data class SensorReportInformation(
    val deviceId: String,
    val date: LocalDate,
    val lastLatitude: Double,
    val lastLongitude: Double,
    val name: String,
    val numberOfPoints: Int
)