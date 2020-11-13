package hpsaturn.pollutionreporter.report.data.mappers

import hpsaturn.pollutionreporter.data.TestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SensorDataPointMapperTest {

    private lateinit var tSensorDataPointMapper: SensorDataPointMapper

    @BeforeEach
    fun setUp() {
        tSensorDataPointMapper = SensorDataPointMapper()
    }

    @Test
    fun `should map TrackData to SensorReportInformation`() {
        // act
        val response = tSensorDataPointMapper(TestData.trackData1)
        // assert
        assertEquals(TestData.trackData1.id, response.pointId)
        assertEquals(TestData.trackData1.p10, response.p10)
        assertEquals(TestData.trackData1.p25, response.p25)
        assertEquals(TestData.trackData1.p25, response.p25)
        assertEquals(TestData.trackData1.spd, response.spd)
        assertEquals(TestData.trackData1.spd, response.spd)
        assertEquals(TestData.trackData1.latitude, response.latitude)
        assertEquals(TestData.trackData1.longitude, response.longitude)
        assertEquals(TestData.trackData1.timestamp, response.timestamp)
    }
}