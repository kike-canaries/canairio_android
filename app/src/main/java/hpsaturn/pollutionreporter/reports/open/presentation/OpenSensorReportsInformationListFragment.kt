package hpsaturn.pollutionreporter.reports.open.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.reports.open.domain.entities.SensorReportInformation
import kotlinx.android.synthetic.main.fragment_sensor_report_information_list.*
import javax.inject.Inject

@AndroidEntryPoint
class OpenSensorReportsInformationListFragment : Fragment() {

    @Inject
    lateinit var sensorReportAdapter: SensorReportAdapter

    private val openSensorReportsViewModel: OpenSensorReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sensor_report_information_list, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadSensorReports()
    }

    private fun initRecyclerView() {
        recordsListRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = sensorReportAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun loadSensorReports() {
        openSensorReportsViewModel.publicReports.observe(viewLifecycleOwner, Observer {
            setAllViewsInvisible()
            when (it) {
                is Success -> renderData(it.data)
                is ErrorResult -> renderError(it.exception)
                is InProgress -> renderProgress()
            }
        })
    }

    private fun renderData(sensorReportsInformation: List<SensorReportInformation>) {
        recordsListRecyclerView.visibility = View.VISIBLE
        sensorReportAdapter.submitList(sensorReportsInformation)
    }

    private fun renderError(exception: Throwable) {
        errorMessage.visibility = View.VISIBLE
        errorMessage.text = exception.message
    }

    private fun renderProgress() {
        openSensorReportsLoadingIndicator.visibility = View.VISIBLE
    }

    private fun setAllViewsInvisible() {
        recordsListRecyclerView.visibility = View.INVISIBLE
        openSensorReportsLoadingIndicator.visibility = View.INVISIBLE
        errorMessage.visibility = View.INVISIBLE
    }

}
