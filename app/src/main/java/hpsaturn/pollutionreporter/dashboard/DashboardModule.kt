package hpsaturn.pollutionreporter.dashboard

import android.location.Location
import androidx.lifecycle.LiveData
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.dashboard.data.mappers.AirQualityStatusMapper
import hpsaturn.pollutionreporter.dashboard.data.models.AqicnFeedResponse
import hpsaturn.pollutionreporter.dashboard.data.repositories.AirQualityStatusRepositoryImpl
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.repositories.AirQualityStatusRepository
import hpsaturn.pollutionreporter.dashboard.presentation.CurrentLocationLiveData
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class DashboardModule {

    @Binds
    abstract fun bindAirQualityStatusMapper(airQualityStatusMapper: AirQualityStatusMapper):
            Mapper<AqicnFeedResponse, AirQualityStatus>

    @Binds
    abstract fun bindAirQualityStatusRepositoryImpl(
        airQualityStatusRepositoryImpl: AirQualityStatusRepositoryImpl
    ): AirQualityStatusRepository

    @Binds
    abstract fun bindCurrentLocationLiveData(currentLocationLiveData: CurrentLocationLiveData):
            LiveData<Result<Location>>

    companion object {
        @Singleton
        @Provides
        fun provideAqicnApiFeedService(retrofit: Retrofit): AqicnApiFeedService =
            retrofit.create(AqicnApiFeedService::class.java)
    }

}