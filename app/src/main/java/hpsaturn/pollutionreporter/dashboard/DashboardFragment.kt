package hpsaturn.pollutionreporter.dashboard

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RotateDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.dashboard.domain.entities.AirQualityStatus
import hpsaturn.pollutionreporter.dashboard.domain.usecases.EvaluateAirQualityStatus
import hpsaturn.pollutionreporter.dashboard.presentation.DashboardViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    @Inject
    lateinit var evaluateAirQualityStatus: EvaluateAirQualityStatus

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayAirQualityIndexOnView()
        displayStationDistance()
    }

    private fun displayAirQualityIndexOnView() {
        dashboardViewModel.airQualityStatus.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> renderAqiData(it.data)
                is ErrorResult -> renderError(it.exception)
                is InProgress -> renderProgress()
            }
        })
    }

    private fun displayStationDistance() {
        dashboardViewModel.distanceToStation.observe(viewLifecycleOwner, Observer {
            currentLocationText.text = "$it Km"
        })
    }

    private fun renderAqiData(airQualityStatus: AirQualityStatus) {
        setTextVisible()
        val scale = evaluateAirQualityStatus(airQualityStatus)
        val background =
            (airQualityIndexBar.progressDrawable as RotateDrawable).drawable as GradientDrawable
        val color = ContextCompat.getColor(requireContext(), scale.colorResourceId)
        background.colors =
            intArrayOf(color, color) // Both the same because we don't have a gradient.
        airQualityIndexText.text = "${airQualityStatus.airQualityIndex}"
        airQualityLabelText.text = getString(scale.nameResourceId)
    }

    private fun renderError(exception: Throwable) {
        setTextVisible()
        airQualityIndexText.text = context?.getString(R.string.error)
        airQualityLabelText.text = "${exception.message}"
    }

    private fun renderProgress() {
        progressBar.visibility = View.VISIBLE
        airQualityIndexText.visibility = View.INVISIBLE
        airQualityLabelText.visibility = View.INVISIBLE
    }

    private fun setTextVisible() {
        progressBar.visibility = View.INVISIBLE
        airQualityIndexText.visibility = View.VISIBLE
        airQualityLabelText.visibility = View.VISIBLE
    }

}
