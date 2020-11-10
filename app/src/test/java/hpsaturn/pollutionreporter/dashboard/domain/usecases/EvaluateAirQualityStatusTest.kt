package hpsaturn.pollutionreporter.dashboard.domain.usecases

import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityScale
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
internal class EvaluateAirQualityStatusTest {
    private lateinit var useCase: EvaluateAirQualityStatus

    @BeforeEach
    fun setUp() {
        useCase = EvaluateAirQualityStatus()
    }

    @Test
    fun `should return GOOD if the AQI is between 0 and 50`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(0, 50))
        // act
        val result = useCase(tAirQualityStatus)
        // assert
        assertEquals(AirQualityScale.GOOD, result)
    }

    @Test
    fun `should return MODERATE if the AQI is between 51 and 100`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(51, 100))
        // act
        val result = useCase(tAirQualityStatus)
        // assert
        assertEquals(AirQualityScale.MODERATE, result)
    }

    @Test
    fun `should return UNHEALTHY_FOR_SENSITIVE_GROUPS if the AQI is between 101 and 150`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(101, 150))
        // act
        val result = useCase(tAirQualityStatus)
        // assert
        assertEquals(AirQualityScale.UNHEALTHY_FOR_SENSITIVE_GROUPS, result)
    }

    @Test
    fun `should return UNHEALTHY if the AQI is between 151 and 200`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(151, 200))
        // act
        val result = useCase(tAirQualityStatus)
        // assert
        assertEquals(AirQualityScale.UNHEALTHY, result)
    }

    @Test
    fun `should return VERY_UNHEALTHY if the AQI is between 201 and 300`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(201, 300))
        // act
        val result = useCase(tAirQualityStatus)
        // assert
        assertEquals(AirQualityScale.VERY_UNHEALTHY, result)
    }

    @Test
    fun `should return HAZARDOUS if the AQI is between 201 and 300`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(301, Int.MAX_VALUE))
        // act
        val result = useCase(tAirQualityStatus)
        // assert
        assertEquals(AirQualityScale.HAZARDOUS, result)
    }

    @Test
    fun `should throw IllegalArgumentException if AQI is negative`() {
        // arrange
        val tAirQualityStatus = generateMockAirQualityStatus(Random.nextInt(Int.MIN_VALUE, -1))
        // assert
        val exception = assertThrows<IllegalArgumentException> {
            // act
            useCase(tAirQualityStatus)
        }
        assertEquals("No negative values for AQI.", exception.message)
    }

    private fun generateMockAirQualityStatus(aqi: Int): AirQualityStatus = AirQualityStatus(
        aqi,
        "station name",
        4.645594,
        -74.058881
    )
}