package hpsaturn.pollutionreporter.reports

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.reports.open.data.mappers.SensorDataPointMapper
import hpsaturn.pollutionreporter.reports.open.data.mappers.SensorReportInformationMapper
import hpsaturn.pollutionreporter.reports.open.data.models.TracksData
import hpsaturn.pollutionreporter.reports.open.data.models.TracksInfo
import hpsaturn.pollutionreporter.reports.open.data.repositories.OpenSensorReportsRepositoryImpl
import hpsaturn.pollutionreporter.reports.open.data.services.PublicSensorReportService
import hpsaturn.pollutionreporter.reports.open.data.services.PublicSensorReportServiceImp
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorDataPoint
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.reports.open.domain.repositories.OpenSensorReportsRepository

@Module
@InstallIn(ApplicationComponent::class)
abstract class SensorReportModule {

    @Binds
    abstract fun bindSensorDataPointMapper(
        sensorDataPointMapper: SensorDataPointMapper
    ): Mapper<TracksData, SensorDataPoint>

    @Binds
    abstract fun bindSensorReportInformationMapper(
        sensorDataPointMapper: SensorReportInformationMapper
    ): Mapper<TracksInfo, SensorReportInformation>

    @Binds
    abstract fun bindSensorReportRepositoryImpl(
        sensorReportRepositoryImpl: OpenSensorReportsRepositoryImpl
    ): OpenSensorReportsRepository

    @Binds
    abstract fun bindPublicSensorReportServiceImpl(
        publicSensorReportServiceImp: PublicSensorReportServiceImp
    ): PublicSensorReportService
}