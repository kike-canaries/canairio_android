package hpsaturn.pollutionreporter.dashboard.data.repositories

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.errors.ServerException
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.fixtures.JsonFixture
import hpsaturn.pollutionreporter.fixtures.readFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.Response

@ExtendWith(MockKExtension::class)
internal class AirQualityStatusRepositoryImplTest {

    private lateinit var repository: AirQualityStatusRepositoryImpl

    @MockK
    private lateinit var mockAqicnApiFeedService: AqicnApiFeedService

    @MockK
    private lateinit var mockMapper: Mapper<AqicnFeedResponse, AirQualityStatus>

    private val tLatitude = 4.645594
    private val tLongitude = -74.058881
    private val tErrorMessage = "Server Error"
    private val tAqicnFeedResponse =
        readFixture(JsonFixture.STATION_FEED, AqicnFeedResponse::class.java)
    private val tAirQualityStatus = AirQualityStatus(
        1,
        "station name",
        tLatitude,
        tLongitude
    )

    @BeforeEach
    fun setUp() {
        repository = AirQualityStatusRepositoryImpl(mockAqicnApiFeedService, mockMapper)
    }

    @Test
    fun `should return remote data when the call to remote data source is ok`() {
        // arrange
        every { mockMapper(any()) } returns tAirQualityStatus
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } returns Response.success<AqicnFeedResponse>(tAqicnFeedResponse)
        // act
        val result = runBlocking { repository.getNearestAirQualityStatus(tLatitude, tLongitude) }
        // assert
        coVerify { mockAqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
        assertEquals(tAirQualityStatus, result)
    }

    @Test
    fun `should throw exception if response null`() {
        // arrange
        every { mockMapper(any()) } returns tAirQualityStatus
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } returns Response.success<AqicnFeedResponse>(200, null)
        // act
        assertThrows<ServerException> {
            runBlocking {
                repository.getNearestAirQualityStatus(tLatitude, tLongitude)
            }
        }
        // assert
        coVerify { mockAqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
    }

    @Test
    fun `should throw exception if response not successful`() {
        // arrange
        every { mockMapper(any()) } returns tAirQualityStatus
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } returns Response.error<AqicnFeedResponse>(400, tErrorMessage.toResponseBody())
        // act
        assertThrows<ServerException> {
            runBlocking {
                repository.getNearestAirQualityStatus(tLatitude, tLongitude)
            }
        }
        // assert
        coVerify { mockAqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
    }
}