package hpsaturn.pollutionreporter.reports.open.data.repositories

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.reports.open.data.models.TracksInfo
import hpsaturn.pollutionreporter.reports.open.data.services.PublicSensorReportService
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.reports.open.domain.repositories.OpenSensorReportsRepository
import javax.inject.Inject

class OpenSensorReportsRepositoryImpl @Inject constructor(
    private val sensorReportService: PublicSensorReportService,
    private val mapper: Mapper<TracksInfo, SensorReportInformation>
) : OpenSensorReportsRepository {
    override suspend fun getPublicSensorReports(): Result<List<SensorReportInformation>> =
        runCatching {
            Success(sensorReportService.getTracksInfo().map { mapper(it) })
        }.getOrElse {
            ErrorResult(it)
        }

}