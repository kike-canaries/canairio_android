package hpsaturn.pollutionreporter.report.data.mappers

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.report.data.models.TracksInfo
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import javax.inject.Inject

class SensorReportInformationMapper @Inject constructor() :
    Mapper<TracksInfo, SensorReportInformation> {
    override fun invoke(input: TracksInfo): SensorReportInformation = SensorReportInformation(
        input.deviceId,
        input.date,
        input.lastLat,
        input.lastLon,
        input.name,
        input.size
    )
}