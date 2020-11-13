package hpsaturn.pollutionreporter.report.data.mappers

import hpsaturn.pollutionreporter.data.TestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SensorReportInformationMapperTest {

    private lateinit var tSensorReportInformationMapper: SensorReportInformationMapper

    @BeforeEach
    fun setUp() {
        tSensorReportInformationMapper = SensorReportInformationMapper()
    }

    @Test
    fun `should map TrackInfo to SensorReportInformation`() {
        // act
        val response = tSensorReportInformationMapper(TestData.trackInformation1)
        // assert
        assertEquals(TestData.trackInformation1.deviceId, response.deviceId)
        assertEquals(TestData.trackInformation1.date, response.date)
        assertEquals(TestData.trackInformation1.lastLat, response.lastLatitude)
        assertEquals(TestData.trackInformation1.lastLon, response.lastLongitude)
        assertEquals(TestData.trackInformation1.name, response.name)
        assertEquals(TestData.trackInformation1.size, response.numberOfPoints)
    }
}