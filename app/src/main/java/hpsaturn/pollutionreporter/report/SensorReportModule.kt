package hpsaturn.pollutionreporter.report

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hpsaturn.pollutionreporter.core.data.mappers.Mapper
import hpsaturn.pollutionreporter.report.data.mappers.SensorDataPointMapper
import hpsaturn.pollutionreporter.report.data.mappers.SensorReportInformationMapper
import hpsaturn.pollutionreporter.report.data.models.TracksData
import hpsaturn.pollutionreporter.report.data.models.TracksInfo
import hpsaturn.pollutionreporter.report.data.repositories.SensorReportRepositoryImpl
import hpsaturn.pollutionreporter.report.data.services.PublicSensorReportService
import hpsaturn.pollutionreporter.report.data.services.PublicSensorReportServiceImp
import hpsaturn.pollutionreporter.report.domain.entities.SensorDataPoint
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.report.domain.repositories.SensorReportRepository

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
        sensorReportRepositoryImpl: SensorReportRepositoryImpl
    ): SensorReportRepository

    @Binds
    abstract fun bindPublicSensorReportServiceImpl(
        publicSensorReportServiceImp: PublicSensorReportServiceImp
    ): PublicSensorReportService
}