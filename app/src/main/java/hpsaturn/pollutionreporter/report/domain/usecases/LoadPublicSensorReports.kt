package hpsaturn.pollutionreporter.report.domain.usecases

import hpsaturn.pollutionreporter.di.DispatchersModule
import hpsaturn.pollutionreporter.report.domain.repositories.SensorReportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoadPublicSensorReports @Inject constructor(
    private val sensorReportRepository: SensorReportRepository,
    @DispatchersModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke() =
        withContext(ioDispatcher) { sensorReportRepository.getPublicSensorReports() }
}