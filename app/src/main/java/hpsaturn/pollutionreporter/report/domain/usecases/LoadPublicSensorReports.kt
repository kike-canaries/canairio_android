package hpsaturn.pollutionreporter.report.domain.usecases

import hpsaturn.pollutionreporter.report.domain.repositories.SensorReportRepository
import javax.inject.Inject

class LoadPublicSensorReports @Inject constructor(
    private val sensorReportRepository: SensorReportRepository
) {
    suspend operator fun invoke() = sensorReportRepository.getPublicSensorReports()
}