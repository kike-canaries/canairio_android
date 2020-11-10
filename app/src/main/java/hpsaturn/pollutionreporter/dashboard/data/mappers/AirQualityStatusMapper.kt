package hpsaturn.pollutionreporter.dashboard.data.mappers

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import javax.inject.Inject

class AirQualityStatusMapper @Inject constructor() : Mapper<AqicnFeedResponse, AirQualityStatus> {
    override fun invoke(input: AqicnFeedResponse): AirQualityStatus = AirQualityStatus(
        input.data.aqi,
        input.data.city.name,
        input.data.city.geo[0],
        input.data.city.geo[1]
    )
}