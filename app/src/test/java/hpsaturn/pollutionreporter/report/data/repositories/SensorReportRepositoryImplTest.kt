package hpsaturn.pollutionreporter.report.data.repositories

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.data.TestData
import hpsaturn.pollutionreporter.report.data.models.TracksInfo
import hpsaturn.pollutionreporter.report.data.services.PublicSensorReportService
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
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

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class SensorReportRepositoryImplTest {
    private lateinit var repository: SensorReportRepositoryImpl

    @MockK
    private lateinit var mockSensorReportService: PublicSensorReportService

    @MockK
    private lateinit var mockMapper: Mapper<TracksInfo, SensorReportInformation>

    @BeforeEach
    fun setUp() {
        repository = SensorReportRepositoryImpl(mockSensorReportService, mockMapper)
    }

    @Test
    fun `should return remote data when the call to remote data source is ok`() = runBlockingTest {
        // arrange
        every { mockMapper(any()) } returnsMany TestData.sensorReportInformationList
        coEvery { mockSensorReportService.getTracksInfo() } returns TestData.trackInformationList
        // act
        val result = repository.getPublicSensorReports()
        // assert
        coVerify { mockSensorReportService.getTracksInfo() }
        verify { mockMapper(TestData.trackInformation1) }
        assertEquals(Success(TestData.sensorReportInformationList), result)
    }

    @Test
    fun `should wrap the in ErrorResponse in case service throws an error`() = runBlockingTest {
        // arrange
        val tException = Exception()
        coEvery { mockSensorReportService.getTracksInfo() } throws tException
        // act
        val result = repository.getPublicSensorReports()
        // assert
        assertEquals(ErrorResult(tException), result)
        verify(exactly = 0) { mockMapper(any()) }
    }
}