package hpsaturn.pollutionreporter.report.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.report.domain.usecases.LoadPublicSensorReports

class SensorReportViewModel @ViewModelInject constructor(
    private val loadPublicSensorReports: LoadPublicSensorReports
) : ViewModel() {
    val publicReports: LiveData<Result<List<SensorReportInformation>>> = liveData {
        emit(loadPublicSensorReports())
    }
}