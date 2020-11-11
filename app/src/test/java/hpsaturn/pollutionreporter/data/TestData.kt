package hpsaturn.pollutionreporter.data

import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import java.time.LocalDate

object TestData {

    private const val latitude1 = 4.645594
    private const val longitude1 = -74.058881

    val sensorReportInformation1 = SensorReportInformation(
        "device1",
        LocalDate.parse("2020-10-23"),
        latitude1,
        longitude1,
        "sensor1",
        3
    )

    val sensorReportInformation2 = SensorReportInformation(
        "device2",
        LocalDate.parse("2020-10-23"),
        latitude1,
        longitude1,
        "sensor2",
        18
    )

    val sensorReportInformation3 = SensorReportInformation(
        "device3",
        LocalDate.parse("2020-10-23"),
        latitude1,
        longitude1,
        "sensor3",
        23
    )

    val sensorReportInformationList = listOf(
        sensorReportInformation1, sensorReportInformation2, sensorReportInformation3
    )
}