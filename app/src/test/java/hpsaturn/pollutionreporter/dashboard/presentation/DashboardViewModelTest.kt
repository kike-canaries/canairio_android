package hpsaturn.pollutionreporter.dashboard.presentation

import android.location.Location
import androidx.lifecycle.MutableLiveData
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.usecases.FindNearestAirQualityStatus
import hpsaturn.pollutionreporter.util.InstantExecutorExtension
import hpsaturn.pollutionreporter.util.MainCoroutineTestExtension
import hpsaturn.pollutionreporter.util.observeForTesting
import hpsaturn.pollutionreporter.util.round
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
internal class DashboardViewModelTest {

    private lateinit var dashboardViewModel: DashboardViewModel

    @MockK
    private lateinit var mockFindNearestAirQualityStatus: FindNearestAirQualityStatus

    @MockK(relaxed = true)
    private lateinit var mockLocation: Location

    private var mockCurrentLocationLiveData = MutableLiveData<Result<Location>>()

    private val tLatitude = 4.645594
    private val tLongitude = -74.058881

    private val tAirQualityStatus = AirQualityStatus(
        1,
        "station name",
        tLatitude,
        tLongitude
    )

    private val tException = Exception()
    private val tDistanceInMeters = 589541F

    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineTestExtension()

    @BeforeEach
    fun setUp() {
        dashboardViewModel = DashboardViewModel(
            mockFindNearestAirQualityStatus,
            mockCurrentLocationLiveData,
            coroutineRule.dispatcher
        )
    }

    @Test
    fun `should emit InProgress if current location returns InProgress when subscribed to airQualityStatus`() =
        coroutineRule.runBlockingTest {
            // act
            dashboardViewModel.airQualityStatus.observeForTesting {
                // arrange
                mockCurrentLocationLiveData.value = InProgress
                // assert
                assertEquals(InProgress, dashboardViewModel.airQualityStatus.value)
            }
        }

    @Test
    fun `should emit ErrorResult if current location returns ErrorResult when subscribed to airQualityStatus`() =
        coroutineRule.runBlockingTest {
            // act
            dashboardViewModel.airQualityStatus.observeForTesting {
                // arrange
                mockCurrentLocationLiveData.value = ErrorResult(tException)
                // assert
                assertEquals(ErrorResult(tException), dashboardViewModel.airQualityStatus.value)
            }
        }

    @Test
    fun `should emit Success and call useCase to fetch nearest AQ station when subscribed to airQualityStatus`() =
        coroutineRule.runBlockingTest {
            // arrange
            every { mockLocation.latitude } returns tLatitude
            every { mockLocation.longitude } returns tLongitude
            coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
            // act
            dashboardViewModel.airQualityStatus.observeForTesting {
                mockCurrentLocationLiveData.value = Success(mockLocation)
                // assert
                assertEquals(Success(tAirQualityStatus), dashboardViewModel.airQualityStatus.value)
                coVerify { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
            }

        }

    @Test
    fun `should emit ErrorResult if useCase throws error when fetching nearest AQ`() =
        coroutineRule.runBlockingTest {
            // arrange
            every { mockLocation.latitude } returns tLatitude
            every { mockLocation.longitude } returns tLongitude
            coEvery { mockFindNearestAirQualityStatus(any(), any()) } throws tException
            // act
            dashboardViewModel.airQualityStatus.observeForTesting {
                mockCurrentLocationLiveData.value = Success(mockLocation)
                coVerify { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
                // assert
                assertEquals(ErrorResult(tException), dashboardViewModel.airQualityStatus.value)
            }

        }

    @Test
    fun `should calculate distance in Km rounded to 2 decimals between current location and station`() =
        coroutineRule.runBlockingTest {
            // arrange
            val distanceInKm = (tDistanceInMeters / 1000F).toDouble().round(2)
            every { mockLocation.latitude } returns tLatitude
            every { mockLocation.longitude } returns tLongitude
            every { mockLocation.distanceTo(any()) } returns tDistanceInMeters
            coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
            // act
            dashboardViewModel.distanceToStation.observeForTesting {
                mockCurrentLocationLiveData.value = Success(mockLocation)
                // assert
                assertEquals(distanceInKm, dashboardViewModel.distanceToStation.value)
                verify { mockLocation.distanceTo(any()) }
            }

        }


    @Test
    fun `should return 0  in Km between current location and station`() =
        coroutineRule.runBlockingTest {
            // arrange
            every { mockLocation.latitude } returns tLatitude
            every { mockLocation.longitude } returns tLongitude
            every { mockLocation.distanceTo(any()) } returns tDistanceInMeters
            coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
            // act
            dashboardViewModel.distanceToStation.observeForTesting {
                mockCurrentLocationLiveData.value = ErrorResult(tException)
                // assert
                assertEquals(0.00, dashboardViewModel.distanceToStation.value)
                verify(exactly = 0) { mockLocation.distanceTo(any()) }
            }

        }

}
