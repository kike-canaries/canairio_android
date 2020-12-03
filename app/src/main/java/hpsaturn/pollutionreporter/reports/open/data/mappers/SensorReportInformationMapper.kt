package hpsaturn.pollutionreporter.reports.open.data.mappers

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.reports.open.data.models.TracksInfo
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorReportInformation
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