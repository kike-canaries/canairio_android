package hpsaturn.pollutionreporter.report.domain.usecases

import hpsaturn.pollutionreporter.data.TestData
import hpsaturn.pollutionreporter.report.domain.repositories.SensorReportRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class LoadPublicSensorReportsTest {

    private lateinit var useCase: LoadPublicSensorReports

    @MockK
    private lateinit var mockSensorReportRepository: SensorReportRepository

    @BeforeEach
    fun setUp() {
        useCase = LoadPublicSensorReports(mockSensorReportRepository)
    }

    @Test
    fun `should call the repository to fetch public reports`() = runBlocking {
        // arrange
        coEvery {
            mockSensorReportRepository.getPublicSensorReports()
        } returns TestData.sensorReportInformationList
        // act
        val result = useCase()
        // assert
        assertEquals(TestData.sensorReportInformationList, result)
        coVerify { mockSensorReportRepository.getPublicSensorReports() }
    }

}