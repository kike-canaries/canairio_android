package hpsaturn.pollutionreporter.reports.open.domain.repositories

import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorReportInformation

interface OpenSensorReportsRepository {
    suspend fun getPublicSensorReports(): Result<List<SensorReportInformation>>
}