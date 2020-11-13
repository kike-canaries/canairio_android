package hpsaturn.pollutionreporter.dashboard.data.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.errors.ConnectionException
import hpsaturn.pollutionreporter.core.domain.errors.ServerException
import hpsaturn.pollutionreporter.core.domain.errors.UnexpectedException
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository
import java.io.IOException
import javax.inject.Inject

class AirQualityStatusRepositoryImpl @Inject constructor(
    private val aqicnApiFeedService: AqicnApiFeedService,
    private val mapper: Mapper<AqicnFeedResponse, AirQualityStatus>,
    @ApplicationContext private val context: Context
) : AirQualityStatusRepository {

    override suspend fun getNearestAirQualityStatus(
        latitude: Double,
        longitude: Double
    ): AirQualityStatus {

        val response = runCatching {
            aqicnApiFeedService.getGeolocationFeed(latitude, longitude)
        }.getOrElse {
            if (it is IOException) throw ConnectionException(context.getString(R.string.internet_connection_unavailable))
            else throw UnexpectedException(context.getString(R.string.unexpected_error))
        }
        val aqicnFeedResponse = response.body()
        if (!response.isSuccessful || aqicnFeedResponse == null) {
            throw ServerException(context.getString(R.string.server_unavailable))
        }
        return mapper(aqicnFeedResponse)
    }
}