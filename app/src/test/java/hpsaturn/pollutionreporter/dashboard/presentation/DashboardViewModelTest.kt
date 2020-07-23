package hpsaturn.pollutionreporter.dashboard.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository
import hpsaturn.pollutionreporter.dashboard.domain.usecases.FindNearestAirQualityStatus
import hpsaturn.pollutionreporter.util.MainCoroutineScopeRule
import hpsaturn.pollutionreporter.util.observeForTesting
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DashboardViewModelTest {
    private lateinit var viewModel: DashboardViewModel

    @MockK
    private lateinit var mockFindNearestAirQualityStatus: FindNearestAirQualityStatus

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

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
        viewModel = DashboardViewModel(mockFindNearestAirQualityStatus)
    }

    @Test
    fun `should call use case to fetch air quality status`() {
        // arrange
        viewModel = DashboardViewModel(mockFindNearestAirQualityStatus)
        coEvery { mockFindNearestAirQualityStatus(any(), any()) } returns tAirQualityStatus
        // act
//        val result = LiveDataTestUtil.getValue(viewModel.currentAirQualityStatus)
        // assert
//        coVerify { mockFindNearestAirQualityStatus(tLatitude, tLongitude) }
        viewModel.currentAirQualityStatus.observeForTesting {
            assertEquals(tAirQualityStatus, viewModel.currentAirQualityStatus.value)
        }
    }

    @ExperimentalCoroutinesApi
    inner class FakeRepository: AirQualityStatusRepository {
        val testDispatcher = TestCoroutineDispatcher()
        override suspend fun getNearestAirQualityStatus(
            latitude: Double,
            longitude: Double
        ) = tAirQualityStatus
    }
}

