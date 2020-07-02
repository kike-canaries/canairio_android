package hpsaturn.pollutionreporter.dashboard.data.mappers

import com.google.gson.Gson
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.fixtures.JsonFixture
import hpsaturn.pollutionreporter.fixtures.readFixture
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class AirQualityStatusMapperTest {

    private val tAqicnFeedResponseJson = readFixture(JsonFixture.STATION_FEED)
    private val tAqicnFeedResponse = Gson().fromJson(tAqicnFeedResponseJson, AqicnFeedResponse::class.java)
    private lateinit var tAirQualityStatusMapper: AirQualityStatusMapper

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