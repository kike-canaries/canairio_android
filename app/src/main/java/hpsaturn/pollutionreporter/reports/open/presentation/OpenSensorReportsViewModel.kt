package hpsaturn.pollutionreporter.reports.open.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorReportInformation
import hpsaturn.pollutionreporter.reports.open.domain.usecases.LoadOpenSensorReports

class OpenSensorReportsViewModel @ViewModelInject constructor(
    private val loadOpenSensorReports: LoadOpenSensorReports
) : ViewModel() {
    val publicReports: LiveData<Result<List<SensorReportInformation>>> = liveData {
        emit(InProgress)
        emit(loadOpenSensorReports())
    }
}