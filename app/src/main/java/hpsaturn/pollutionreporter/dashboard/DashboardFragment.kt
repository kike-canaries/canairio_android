package hpsaturn.pollutionreporter.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val TAG = DashboardFragment::class.java.simpleName

    @Inject
    lateinit var aqicnApiFeedService: AqicnApiFeedService

    private var coroutineJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "HELLOO")
        Log.d(TAG, TAG)
        val job = CoroutineScope(Dispatchers.IO).launch {
            val response = aqicnApiFeedService.getGeolocationFeed(4.645594, -74.058881)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) Log.d(TAG, "Success")
                Log.d(TAG, response.body()?.data?.aqi.toString())
            }
        }

        job.start()

        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

}
