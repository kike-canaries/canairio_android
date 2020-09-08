package hpsaturn.pollutionreporter.dashboard.presentation

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.usecases.FindNearestAirQualityStatus
import hpsaturn.pollutionreporter.di.DispatchersModule.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AirQualityStatusLiveData @Inject constructor(
    private val findNearestAirQualityStatus: FindNearestAirQualityStatus,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LiveData<AirQualityStatus>() {

    private val setAirQualityStatusListener = { location: Location ->
        CoroutineScope(ioDispatcher).launch {
            postValue(findNearestAirQualityStatus(location.latitude, location.longitude))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            it.also { setAirQualityStatusListener(it) }
        }
        startLocationUpdates()
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                setAirQualityStatusListener(location)
            }
        }
    }

    @SuppressLint("MissingPermission") // TODO -- Check
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

}