package hpsaturn.pollutionreporter.dashboard.data.mappers

import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.fixtures.JsonFixture
import hpsaturn.pollutionreporter.fixtures.readFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AirQualityStatusMapperTest {

    private lateinit var tAirQualityStatusMapper: AirQualityStatusMapper

    private val tAqicnFeedResponse =
        readFixture(JsonFixture.STATION_FEED, AqicnFeedResponse::class.java)

    @BeforeEach
    internal fun setUp() {
        tAirQualityStatusMapper = AirQualityStatusMapper()
    }

    @Test
    fun `should map aqicn response to air quality entity class`() {
        // act
        val response = tAirQualityStatusMapper(tAqicnFeedResponse)
        // assert
        assertEquals(tAqicnFeedResponse.data.aqi, response.airQualityIndex)
        assertEquals(tAqicnFeedResponse.data.city.name, response.stationName)
        assertEquals(tAqicnFeedResponse.data.city.geo[0], response.stationLatitude)
        assertEquals(tAqicnFeedResponse.data.city.geo[1], response.stationLongitude)
    }
}