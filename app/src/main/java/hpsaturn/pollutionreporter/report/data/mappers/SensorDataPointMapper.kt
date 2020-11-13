package hpsaturn.pollutionreporter.report.data.mappers

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.report.data.models.TracksData
import hpsaturn.pollutionreporter.report.domain.entities.SensorDataPoint
import javax.inject.Inject

class SensorDataPointMapper @Inject constructor() : Mapper<TracksData, SensorDataPoint> {
    override fun invoke(input: TracksData): SensorDataPoint = SensorDataPoint(
        input.id,
        input.p10,
        input.p25,
        input.spd,
        input.latitude,
        input.longitude,
        input.timestamp
    )
}