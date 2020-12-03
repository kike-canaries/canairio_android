package hpsaturn.pollutionreporter.reports.open.data.mappers

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.reports.open.data.models.TracksData
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorDataPoint
import hpsaturn.pollutionreporter.util.toUnixTimeStamp
import javax.inject.Inject

class SensorDataPointMapper @Inject constructor() : Mapper<TracksData, SensorDataPoint> {
    override fun invoke(input: TracksData): SensorDataPoint = SensorDataPoint(
        input.id,
        input.p10,
        input.p25,
        input.spd,
        input.latitude,
        input.longitude,
        input.timestamp.toUnixTimeStamp()
    )
}