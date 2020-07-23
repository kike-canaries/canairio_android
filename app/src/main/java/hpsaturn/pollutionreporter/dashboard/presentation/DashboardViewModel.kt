package hpsaturn.pollutionreporter.dashboard.presentation

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.usecases.FindNearestAirQualityStatus

class DashboardViewModel @ViewModelInject constructor(
    private val findNearestAirQualityStatus: FindNearestAirQualityStatus
) : ViewModel() {
    val currentAirQualityStatus: LiveData<AirQualityStatus> = liveData {
        Log.d("DashboardViewModel", "Called")
        val data = findNearestAirQualityStatus(4.645594, -74.058881)
        emit(data)
    }

}