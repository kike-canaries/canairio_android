package hpsaturn.pollutionreporter.reports.open.data.services

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import hpsaturn.pollutionreporter.reports.open.data.models.TracksInfo
import hpsaturn.pollutionreporter.reports.open.domain.entities.TracksInfoNotFoundException
import hpsaturn.pollutionreporter.util.getSuspendValue
import javax.inject.Inject

interface PublicSensorReportService {
    suspend fun getTracksInfo(): List<TracksInfo>
}

class PublicSensorReportServiceImp @Inject constructor(
    private val database: DatabaseReference
) : PublicSensorReportService {

    override suspend fun getTracksInfo(): List<TracksInfo> {
        val result = database
            .child(TRACKS_INFO_COLLECTION)
            .limitToLast(20)
            .getSuspendValue()
            .getValue(object :
                GenericTypeIndicator<Map<String, @JvmSuppressWildcards TracksInfo>>() {})

        return result?.values?.toList() ?: throw TracksInfoNotFoundException()
    }

    companion object {
        private const val TRACKS_INFO_COLLECTION = "tracks_info"
    }

}