package hpsaturn.pollutionreporter.dashboard.presentation

import android.Manifest
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.Tasks
import com.karumi.dexter.DexterBuilder
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.util.AutoSuccessTask
import hpsaturn.pollutionreporter.util.InstantExecutorExtension
import hpsaturn.pollutionreporter.util.getOrAwaitValueTest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions

@Extensions(ExtendWith(MockKExtension::class), ExtendWith(InstantExecutorExtension::class))
internal class CurrentLocationLiveDataTest {

    private lateinit var currentLocationLiveData: CurrentLocationLiveData

    @MockK(relaxed = true)
    private lateinit var mockFusedLocationProviderClient: FusedLocationProviderClient

    @MockK(relaxed = true)
    private lateinit var mockLocationRequest: LocationRequest

    @MockK(relaxed = true)
    private lateinit var mockLocation: Location

    @MockK(relaxed = true)
    private lateinit var mockDexter: DexterBuilder.Permission

    @MockK(relaxed = true)
    private lateinit var mockContext: Context

    private val tLatitude = 4.645594
    private val tLongitude = -74.058881

    @BeforeEach
    fun setUp() {
        currentLocationLiveData = CurrentLocationLiveData(
            mockFusedLocationProviderClient,
            mockLocationRequest,
            mockDexter,
            mockContext
        )
    }

    @Test
    fun `should post a Success last location when FusedLocationProvider found one`() {
        // arrange
        val task = AutoSuccessTask(mockLocation)
        every { mockLocation.latitude } returns tLatitude
        every { mockLocation.longitude } returns tLongitude
        every { mockFusedLocationProviderClient.lastLocation } returns task
        mockFusedLocationProviderClient.lastLocation.result
        // act
        currentLocationLiveData.getOrAwaitValueTest {
            val data = currentLocationLiveData.value
            // assert
            verify { mockFusedLocationProviderClient.lastLocation }
            assertEquals(Success(mockLocation), data)
        }
    }


    @Test
    fun `should remove location updates after getting last location`() {
        // arrange
        val task = AutoSuccessTask(mockLocation)
        every { mockLocation.latitude } returns tLatitude
        every { mockLocation.longitude } returns tLongitude
        every { mockFusedLocationProviderClient.lastLocation } returns task
        mockFusedLocationProviderClient.lastLocation.result
        // act
        currentLocationLiveData.getOrAwaitValueTest()
        // assert
        verify { mockFusedLocationProviderClient.removeLocationUpdates(any() as LocationCallback) }
    }

    @Test
    fun `should post in progress value when first subscribed to`() {
        // arrange
        val task = Tasks.forCanceled<Location>()
        every { mockFusedLocationProviderClient.lastLocation } returns task
        // act
        val data = currentLocationLiveData.getOrAwaitValueTest()
        // assert
        assertEquals(InProgress, data)
    }

    @Test
    fun `should request location permissions when first subscribed to`() {
        // arrange
        val task = AutoSuccessTask(mockLocation)
        every { mockLocation.latitude } returns tLatitude
        every { mockLocation.longitude } returns tLongitude
        every { mockFusedLocationProviderClient.lastLocation } returns task
        // act
        currentLocationLiveData.getOrAwaitValueTest()
        // assert
        verify { mockFusedLocationProviderClient.removeLocationUpdates(any() as LocationCallback) }
        verify {
            mockDexter.withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

}