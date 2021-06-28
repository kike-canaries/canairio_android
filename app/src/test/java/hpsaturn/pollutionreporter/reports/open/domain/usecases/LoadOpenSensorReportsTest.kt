package hpsaturn.pollutionreporter.reports.open.domain.usecases

import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.data.TestData
import hpsaturn.pollutionreporter.reports.open.domain.repositories.OpenSensorReportsRepository
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
internal class LoadOpenSensorReportsTest {

    private lateinit var useCase: LoadOpenSensorReports

    @MockK
    private lateinit var mockOpenSensorReportsRepository: OpenSensorReportsRepository

    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineTestExtension()

    @BeforeEach
    fun setUp() {
        useCase = LoadOpenSensorReports(mockOpenSensorReportsRepository, coroutineRule.dispatcher)
    }

    @Test
    fun `should call the repository to fetch public reports`() = coroutineRule.runBlockingTest {
        // arrange
        coEvery {
            mockOpenSensorReportsRepository.getPublicSensorReports()
        } returns Success(TestData.sensorReportInformationList)
        // act
        val result = useCase()
        // assert
        assertEquals(Success(TestData.sensorReportInformationList), result)
        coVerify { mockOpenSensorReportsRepository.getPublicSensorReports() }
    }

}