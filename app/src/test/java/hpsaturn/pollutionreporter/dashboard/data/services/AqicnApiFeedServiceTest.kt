package hpsaturn.pollutionreporter.dashboard.data.services

import hpsaturn.pollutionreporter.core.data.services.getRetrofitTestInstance
import hpsaturn.pollutionreporter.fixtures.JsonFixture
import hpsaturn.pollutionreporter.fixtures.readFixture
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection

internal class AqicnApiFeedServiceTest {

    private val tLatitude = 4.645594
    private val tLongitude = -74.058881

    private var mockWebServer = MockWebServer()
    private lateinit var aqicnApiFeedService: AqicnApiFeedService

    @BeforeEach
    fun setup() {
        mockWebServer.start()
        aqicnApiFeedService = getRetrofitTestInstance(mockWebServer)
                .create(AqicnApiFeedService::class.java)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should make GET request to geo endpoint with latitude and longitude`() {
        // arrange
        val mockResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(readFixture(JsonFixture.STATION_FEED))
        mockWebServer.enqueue(mockResponse)
        // act
        val result = runBlocking { aqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
        val lastRequest = mockWebServer.takeRequest()
        // assert
        assertNotNull(result.body())
        assertNotNull(lastRequest)
        assertNotNull(lastRequest.requestUrl)
        assertEquals("GET", lastRequest.method)
        assertEquals(1, mockWebServer.requestCount)
        assertEquals("feed", lastRequest.requestUrl!!.pathSegments[0])
        assertEquals("geo:$tLatitude;$tLongitude", lastRequest.requestUrl!!.pathSegments[1])
    }

    @Test
    fun `should deserialize JSON to AqicnFeedResponse`() {
        // arrange
        val mockResponse = MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(readFixture(JsonFixture.STATION_FEED))
        mockWebServer.enqueue(mockResponse)
        // act
        val result = runBlocking { aqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
        val lastRequest = mockWebServer.takeRequest()
        // assert
        assertNotNull(result.body())
        assertEquals("ok", result.body()!!.status)
        assertEquals(11, result.body()!!.data.aqi)
        assertEquals(6236, result.body()!!.data.idx)
        assertEquals(4.5725, result.body()!!.data.city.geo[0])
        assertEquals(-74.0836, result.body()!!.data.city.geo[1])
    }
}