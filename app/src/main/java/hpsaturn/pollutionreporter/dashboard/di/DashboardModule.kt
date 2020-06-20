package hpsaturn.pollutionreporter.dashboard.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.dashboard.data.mappers.AirQualityStatusMapper
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus

@Module
@InstallIn(ActivityComponent::class)
abstract class DashboardModule {

    @Binds
    abstract fun bindAirQualityStatusMapper(airQualityStatusMapper: AirQualityStatusMapper):
            Mapper<AqicnFeedResponse, AirQualityStatus>
}