package hpsaturn.pollutionreporter.report.domain.repositories

import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation

interface SensorReportRepository {
    suspend fun getPublicSensorReports(): Result<List<SensorReportInformation>>
}