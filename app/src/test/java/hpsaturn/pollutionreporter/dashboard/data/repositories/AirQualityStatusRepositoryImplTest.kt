package hpsaturn.pollutionreporter.dashboard.data.repositories

import android.content.Context
import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.errors.ConnectionException
import hpsaturn.pollutionreporter.core.domain.errors.ServerException
import hpsaturn.pollutionreporter.core.domain.errors.UnexpectedException
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.Response

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class AirQualityStatusRepositoryImplTest {

    private lateinit var repository: AirQualityStatusRepositoryImpl

    @MockK
    private lateinit var mockAqicnApiFeedService: AqicnApiFeedService

    @MockK
    private lateinit var mockMapper: Mapper<AqicnFeedResponse, AirQualityStatus>

    @MockK(relaxed = true)
    private lateinit var mockContext: Context

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
        repository = AirQualityStatusRepositoryImpl(
            mockAqicnApiFeedService,
            mockMapper,
            mockContext
        )
    }

    @Test
    fun `should return remote data when the call to remote data source is ok`() = runBlockingTest {
        // arrange
        every { mockMapper(any()) } returns tAirQualityStatus
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } returns Response.success<AqicnFeedResponse>(tAqicnFeedResponse)
        // act
        val result = repository.getNearestAirQualityStatus(tLatitude, tLongitude)
        // assert
        coVerify { mockAqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
        assertEquals(tAirQualityStatus, result)

    }

    @Test
    fun `should return error if response null`() = runBlockingTest {
        // arrange
        val response = Response.success<AqicnFeedResponse>(200, null)
        every { mockMapper(any()) } returns tAirQualityStatus
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } returns response
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
    fun `should throw exception if response not successful`() = runBlockingTest {
        // arrange
        val response = Response.error<AqicnFeedResponse>(400, tErrorMessage.toResponseBody())
        every { mockMapper(any()) } returns tAirQualityStatus
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } returns response
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
    fun `should throw ConnectionException if service throws IOException`() = runBlockingTest {
        // arrange
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } throws IOException()
        // act
        assertThrows<ConnectionException> {
            runBlocking {
                repository.getNearestAirQualityStatus(tLatitude, tLongitude)
            }
        }
        // assert
        coVerify { mockAqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
    }

    @Test
    fun `should throw UnexpectedException if service throws unknown exception`() = runBlockingTest {
        // arrange
        coEvery {
            mockAqicnApiFeedService.getGeolocationFeed(
                any(),
                any()
            )
        } throws Exception()
        // act
        assertThrows<UnexpectedException> {
            runBlocking {
                repository.getNearestAirQualityStatus(tLatitude, tLongitude)
            }
        }
        // assert
        coVerify { mockAqicnApiFeedService.getGeolocationFeed(tLatitude, tLongitude) }
    }
}