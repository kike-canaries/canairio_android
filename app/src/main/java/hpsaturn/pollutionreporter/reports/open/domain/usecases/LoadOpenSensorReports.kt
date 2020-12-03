package hpsaturn.pollutionreporter.reports.open.domain.usecases

import hpsaturn.pollutionreporter.di.DispatchersModule
import hpsaturn.pollutionreporter.reports.open.domain.repositories.OpenSensorReportsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Loads public sensor reports from the backend.
 */
class LoadOpenSensorReports @Inject constructor(
    private val openSensorReportsRepository: OpenSensorReportsRepository,
    @DispatchersModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke() =
        withContext(ioDispatcher) { openSensorReportsRepository.getPublicSensorReports() }
}