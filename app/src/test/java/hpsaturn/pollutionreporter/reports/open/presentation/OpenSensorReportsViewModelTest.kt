package hpsaturn.pollutionreporter.reports.open.presentation

import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.data.TestData
import hpsaturn.pollutionreporter.reports.open.domain.usecases.LoadOpenSensorReports
import hpsaturn.pollutionreporter.util.InstantExecutorExtension
import hpsaturn.pollutionreporter.util.getValueForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions

@ExperimentalCoroutinesApi
@Extensions(ExtendWith(MockKExtension::class), ExtendWith(InstantExecutorExtension::class))
internal class OpenSensorReportsViewModelTest {

    private lateinit var openSensorReportsViewModel: OpenSensorReportsViewModel

    @MockK
    private lateinit var mockLoadOpenSensorReports: LoadOpenSensorReports

    private val tException = Exception()

    @BeforeEach
    fun setUp() {
        openSensorReportsViewModel = OpenSensorReportsViewModel(mockLoadOpenSensorReports)
    }

    @Test
    fun `should emit Success with the loaded public sensor report data`() {
        // arrange
        coEvery { mockLoadOpenSensorReports.invoke() } returns Success(TestData.sensorReportInformationList)
        // act
        val result = openSensorReportsViewModel.publicReports.getValueForTest()
        // assert
        assertEquals(Success(TestData.sensorReportInformationList), result)
        coVerify { mockLoadOpenSensorReports() }
    }

    @Test
    fun `should emit InProgress if LoadPublicSensorReports returns InProgress`() {
        // arrange
        coEvery { mockLoadOpenSensorReports.invoke() } returns InProgress
        // act
        val result = openSensorReportsViewModel.publicReports.getValueForTest()
        // assert
        assertEquals(InProgress, result)
        coVerify { mockLoadOpenSensorReports() }
    }

    @Test
    fun `should emit ErrorResult if LoadPublicSensorReports returns ErrorResult`() {
        // arrange
        coEvery { mockLoadOpenSensorReports.invoke() } returns ErrorResult(tException)
        // act
        val result = openSensorReportsViewModel.publicReports.getValueForTest()
        // assert
        assertEquals(ErrorResult(tException), result)
        coVerify { mockLoadOpenSensorReports() }
    }


}