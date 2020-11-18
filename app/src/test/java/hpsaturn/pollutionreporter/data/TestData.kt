package hpsaturn.pollutionreporter.data

import hpsaturn.pollutionreporter.report.data.models.TracksData
import hpsaturn.pollutionreporter.report.data.models.TracksInfo
import hpsaturn.pollutionreporter.report.domain.entities.SensorDataPoint
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.util.toUnixTimeStamp

object TestData {

    private const val latitude1 = 4.645594
    private const val longitude1 = -74.058881

    private const val localDate1 = "Oct, Thu 03"

    private const val unixTimeStamp = 1605631331L
    private val timestamp = unixTimeStamp.toUnixTimeStamp()

    val sensorReportInformation1 = SensorReportInformation(
        "device1",
        localDate1,
        latitude1,
        longitude1,
        "sensor1",
        3
    )

    val sensorReportInformation2 = SensorReportInformation(
        "device2",
        localDate1,
        latitude1,
        longitude1,
        "sensor2",
        18
    )

    val sensorReportInformation3 = SensorReportInformation(
        "device3",
        localDate1,
        latitude1,
        longitude1,
        "sensor3",
        23
    )

    val sensorReportInformationList = listOf(
        sensorReportInformation1, sensorReportInformation2, sensorReportInformation3
    )

    val sensorDataPoint1 = SensorDataPoint(
        "track1",
        1.9,
        2.89,
        5.1,
        latitude1,
        longitude1,
        timestamp
    )

    val trackData1 = TracksData(
        "track1",
        1469.2,
        1.9,
        2.89,
        5.1,
        latitude1,
        longitude1,
        unixTimeStamp
    )

    val trackInformation1 = TracksInfo(
        localDate1,
        "device1",
        latitude1,
        longitude1,
        trackData1,
        "sensor1",
        3
    )

    val trackInformation2 = TracksInfo(
        localDate1,
        "device2",
        latitude1,
        longitude1,
        trackData1,
        "sensor2",
        18
    )

    val trackInformation3 = TracksInfo(
        localDate1,
        "device3",
        latitude1,
        longitude1,
        trackData1,
        "sensor3",
        23
    )

    val trackInformationList = listOf(trackInformation1, trackInformation2, trackInformation3)
}