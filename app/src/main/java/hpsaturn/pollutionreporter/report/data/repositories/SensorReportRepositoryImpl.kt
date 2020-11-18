package hpsaturn.pollutionreporter.report.data.repositories

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.report.data.models.TracksInfo
import hpsaturn.pollutionreporter.report.data.services.PublicSensorReportService
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.report.domain.repositories.SensorReportRepository
import javax.inject.Inject

class SensorReportRepositoryImpl @Inject constructor(
    private val sensorReportService: PublicSensorReportService,
    private val mapper: Mapper<TracksInfo, SensorReportInformation>
) : SensorReportRepository {
    override suspend fun getPublicSensorReports(): Result<List<SensorReportInformation>> =
        runCatching {
            Success(sensorReportService.getTracksInfo().map { mapper(it) })
        }.getOrElse {
            ErrorResult(it)
        }

}