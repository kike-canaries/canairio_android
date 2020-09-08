package hpsaturn.pollutionreporter.dashboard.presentation

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.usecases.FindNearestAirQualityStatus
import hpsaturn.pollutionreporter.util.AutoSuccessTask
import hpsaturn.pollutionreporter.util.InstantExecutorExtension
import hpsaturn.pollutionreporter.util.MainCoroutineTestExtension
import hpsaturn.pollutionreporter.util.getOrAwaitValueTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@Extensions(ExtendWith(MockKExtension::class), ExtendWith(InstantExecutorExtension::class))
internal class AirQualityStatusLiveDataTest {

    private lateinit var airQualityStatusLiveData: AirQualityStatusLiveData

    @MockK
    private lateinit var mockFindNearestAirQualityStatus: FindNearestAirQualityStatus

    @MockK(relaxed = true)
    private lateinit var mockFusedLocationProviderClient: FusedLocationProviderClient

    @MockK(relaxed = true)
    private lateinit var mockLocationRequest: LocationRequest

    @MockK(relaxed = true)
    private lateinit var mockLocation: Location

    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineTestExtension()

    private val tLatitude = 4.645594
    private val tLongitude = -74.058881

    private val tAirQualityStatus = AirQualityStatus(
        1,
        "station name",
        tLatitude,
        tLongitude
    )

    @BeforeEach
    fun setUp() {
        airQualityStatusLiveData = AirQualityStatusLiveData(
            mockFindNearestAirQualityStatus,
            mockFusedLocationProviderClient, mockLocationRequest, coroutineRule.dispatcher
        )
    }

    @Test
    fun `should call repository with fetch location to get nearest AQI`() =
        coroutineRule.runBlockingTest {
            // arrange
            val task = AutoSuccessTask(mockLocation)
            every { mockLocation.latitude } returns tLatitude
            every { mockLocation.longitude } returns tLongitude
            every { mockFusedLocationProviderClient.lastLocation } returns task
            coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
            mockFusedLocationProviderClient.lastLocation.result
            // act
            val data = airQualityStatusLiveData.getOrAwaitValueTest()
            // assert
            coVerify { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
            verify { mockFusedLocationProviderClient.requestLocationUpdates(any(), any(), null) }
            assertEquals(tAirQualityStatus, data)
        }

    @Test
    fun `should remove location updates after getting nearest AQI`() =
        coroutineRule.runBlockingTest {
            // arrange
            val task = AutoSuccessTask(mockLocation)
            every { mockLocation.latitude } returns tLatitude
            every { mockLocation.longitude } returns tLongitude
            every { mockFusedLocationProviderClient.lastLocation } returns task
            coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
            mockFusedLocationProviderClient.lastLocation.result
            // act
            airQualityStatusLiveData.getOrAwaitValueTest()
            // assert
            verify { mockFusedLocationProviderClient.removeLocationUpdates(any() as LocationCallback) }
        }

}