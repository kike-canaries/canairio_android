package hpsaturn.pollutionreporter.dashboard.presentation

import android.Manifest
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.Tasks
import com.karumi.dexter.DexterBuilder.Permission
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Success
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

    @MockK(relaxed = true)
    private lateinit var mockDexter: Permission

    @MockK(relaxed = true)
    private lateinit var mockContext: Context

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
            mockFusedLocationProviderClient,
            mockLocationRequest,
            mockDexter,
            coroutineRule.dispatcher,
            mockContext
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
            airQualityStatusLiveData.getOrAwaitValueTest {
                val data = airQualityStatusLiveData.value
                // assert
                coVerify { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
                assertEquals(Success(tAirQualityStatus), data)
            }
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

    @Test
    fun `should post in progress value when first subscribed to`() {
        // arrange
        val task = Tasks.forCanceled<Location>()
        every { mockFusedLocationProviderClient.lastLocation } returns task
        // act
        val data = airQualityStatusLiveData.getOrAwaitValueTest()
        // assert
        coVerify(exactly = 0) { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
        assertEquals(InProgress, data)
    }


    @Test
    fun `should request location permissions when first subscribed to`() {
        // arrange
        val task = AutoSuccessTask(mockLocation)
        every { mockLocation.latitude } returns tLatitude
        every { mockLocation.longitude } returns tLongitude
        every { mockFusedLocationProviderClient.lastLocation } returns task
        coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
        // act
        airQualityStatusLiveData.getOrAwaitValueTest()
        // assert
        verify { mockFusedLocationProviderClient.removeLocationUpdates(any() as LocationCallback) }
        verify {
            mockDexter.withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    @Test
    fun `should post ErrorResult if exception is thrown by use case`() {
        // arrange
        val task = AutoSuccessTask(mockLocation)
        every { mockLocation.latitude } returns tLatitude
        every { mockLocation.longitude } returns tLongitude
        every { mockFusedLocationProviderClient.lastLocation } returns task
        val exception = Exception()
        coEvery { mockFindNearestAirQualityStatus(any(), any()) } throws exception
        // act
        airQualityStatusLiveData.getOrAwaitValueTest {
            val data = airQualityStatusLiveData.value
            // assert
            coVerify { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
            assertEquals(ErrorResult(exception), data)
        }
    }

}