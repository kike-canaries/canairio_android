package hpsaturn.pollutionreporter.dashboard.presentation

import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.usecases.FindNearestAirQualityStatus
import hpsaturn.pollutionreporter.di.DispatchersModule
import hpsaturn.pollutionreporter.util.combineWith
import hpsaturn.pollutionreporter.util.round
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DashboardViewModel @ViewModelInject constructor(
    private val findNearestAirQualityStatus: FindNearestAirQualityStatus,
    currentLocationLiveData: LiveData<Result<Location>>,
    @DispatchersModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val airQualityStatus: LiveData<Result<AirQualityStatus>> = currentLocationLiveData.switchMap {
        liveData {
            when (it) {
                is Success -> emit(resolveResult(it.data))
                is ErrorResult -> emit(ErrorResult(it.exception))
                is InProgress -> emit(InProgress)
            }
        }
    }

    private val numberOfDecimals = 2
    private val metersInOneKilometer = 1000.0

    private val calculateDistanceInKm =
        { location: Result<Location>?, aqi: Result<AirQualityStatus>? ->
            val aqiLocation = Location("")
            if (location is Success && aqi is Success) {
                aqiLocation.longitude = aqi.data.stationLongitude
                aqiLocation.latitude = aqi.data.stationLatitude
                val distance = location.data.distanceTo(aqiLocation).toDouble() // Result in meters.
                (distance / metersInOneKilometer).round(numberOfDecimals)
            } else {
                0.0
            }
        }

    val distanceToStation: LiveData<Double> =
        currentLocationLiveData.combineWith(airQualityStatus, calculateDistanceInKm)

    private suspend fun resolveResult(location: Location): Result<AirQualityStatus> =
        withContext(ioDispatcher) {
            runCatching {
                Success(findNearestAirQualityStatus(location.latitude, location.longitude))
            }.getOrElse { e -> ErrorResult(e) }
        }


}