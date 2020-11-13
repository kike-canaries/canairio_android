package hpsaturn.pollutionreporter.report.data.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import hpsaturn.pollutionreporter.data.TestData
import hpsaturn.pollutionreporter.report.data.models.TracksInfo
import hpsaturn.pollutionreporter.util.InstantExecutorExtension
import hpsaturn.pollutionreporter.util.MainCoroutineTestExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
@Extensions(ExtendWith(MockKExtension::class), ExtendWith(InstantExecutorExtension::class))
internal class PublicSensorReportServiceImpTest {

    private lateinit var service: PublicSensorReportServiceImp

    @MockK
    private lateinit var mockDatabaseReference: DatabaseReference

    @MockK
    private lateinit var mockDataSnapshot: DataSnapshot

    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineTestExtension()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(coroutineRule.dispatcher)
        service = PublicSensorReportServiceImp(mockDatabaseReference)
    }

    @Test
    fun `should call database to fetch tracks info`() {
        // arrange
        every { mockDataSnapshot.getValue(any() as GenericTypeIndicator<List<TracksInfo>>) } returns TestData
            .trackInformationList
        every { mockDatabaseReference.child("tracks_info") } returns mockDatabaseReference
//        coEvery { mockDatabaseReference.getSuspendValue() } returns mockDataSnapshot
        TODO("Figure out how to test this function.")
        // act
//        val result = service.getTracksInfo()
        // assert
//        assertEquals(TestData.sensorReportInformationList, result)
//        verify { mockDatabaseReference.child("tracks_info") }
//        coVerify { mockDatabaseReference.getSuspendValue() }
    }

}