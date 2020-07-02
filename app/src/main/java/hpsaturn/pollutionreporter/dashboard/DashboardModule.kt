package hpsaturn.pollutionreporter.dashboard

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class) // TODO - Check right scope.
object DashboardModule {

    @Singleton
    @Provides
    fun provideAqicnApiFeedService(retrofit: Retrofit): AqicnApiFeedService =
        retrofit.create(AqicnApiFeedService::class.java)
}