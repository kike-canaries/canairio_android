package hpsaturn.pollutionreporter.dashboard.data.repositories

import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.errors.ServerException
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository
import javax.inject.Inject

class AirQualityStatusRepositoryImpl @Inject constructor(
    private val aqicnApiFeedService: AqicnApiFeedService,
    private val mapper: Mapper<AqicnFeedResponse, AirQualityStatus>
) : AirQualityStatusRepository {

    override suspend fun getNearestAirQualityStatus(
        latitude: Double,
        longitude: Double
    ): AirQualityStatus {
        val response = aqicnApiFeedService.getGeolocationFeed(latitude, longitude)
        val aqicnFeedResponse = response.body()
        if (!response.isSuccessful || aqicnFeedResponse == null) throw ServerException()
        return mapper(aqicnFeedResponse)
    }
}