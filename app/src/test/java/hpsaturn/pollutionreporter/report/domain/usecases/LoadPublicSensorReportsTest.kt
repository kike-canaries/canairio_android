package hpsaturn.pollutionreporter.report.domain.usecases

import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.data.TestData
import hpsaturn.pollutionreporter.report.domain.repositories.SensorReportRepository
import hpsaturn.pollutionreporter.util.InstantExecutorExtension
import hpsaturn.pollutionreporter.util.MainCoroutineTestExtension
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
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@Extensions(ExtendWith(MockKExtension::class), ExtendWith(InstantExecutorExtension::class))
internal class LoadPublicSensorReportsTest {

    private lateinit var useCase: LoadPublicSensorReports

    @MockK
    private lateinit var mockSensorReportRepository: SensorReportRepository

    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineTestExtension()

    @BeforeEach
    fun setUp() {
        useCase = LoadPublicSensorReports(mockSensorReportRepository, coroutineRule.dispatcher)
    }

    @Test
    fun `should call the repository to fetch public reports`() = coroutineRule.runBlockingTest {
        // arrange
        coEvery {
            mockSensorReportRepository.getPublicSensorReports()
        } returns Success(TestData.sensorReportInformationList)
        // act
        val result = useCase()
        // assert
        assertEquals(Success(TestData.sensorReportInformationList), result)
        coVerify { mockSensorReportRepository.getPublicSensorReports() }
    }

}