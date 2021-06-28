package hpsaturn.pollutionreporter.dashboard.domain.usecases

import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class FindNearestAirQualityStatusTest {

    private lateinit var useCase: FindNearestAirQualityStatus

    @MockK
    private lateinit var mockAirQualityStatusRepository: AirQualityStatusRepository

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
        useCase = FindNearestAirQualityStatus(mockAirQualityStatusRepository)
    }

    @Test
    fun `should call the repository to fetch the nearest air quality given coordinates`() =
        runBlockingTest {
            // arrange
            coEvery {
                mockAirQualityStatusRepository.getNearestAirQualityStatus(
                    any(),
                    any()
                )
            } returns tAirQualityStatus
            // act
            val result = useCase(tLatitude, tLongitude)
            // assert
            assertEquals(tAirQualityStatus, result)
            coVerify {
                mockAirQualityStatusRepository.getNearestAirQualityStatus(
                    tLatitude,
                    tLongitude
                )
            }
        }
}