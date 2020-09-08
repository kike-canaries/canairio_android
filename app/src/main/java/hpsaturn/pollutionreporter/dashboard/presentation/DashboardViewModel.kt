package hpsaturn.pollutionreporter.dashboard.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

class DashboardViewModel @ViewModelInject constructor(
    val airQualityStatusLiveData: AirQualityStatusLiveData
) : ViewModel()