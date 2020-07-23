package hpsaturn.pollutionreporter.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import hpsaturn.pollutionreporter.dashboard.presentation.DashboardViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val TAG = DashboardFragment::class.java.simpleName

    private val dashboardViewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var aqicnApiFeedService: AqicnApiFeedService

    private var coroutineJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dashboardViewModel.currentAirQualityStatus.observe(viewLifecycleOwner, Observer {
            airQualittyIdexText.text = "${it.airQualityIndex}"
        })

        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

}
