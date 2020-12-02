package hpsaturn.pollutionreporter.report.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.Success
import kotlinx.android.synthetic.main.fragment_sensor_report_information_list.*
import javax.inject.Inject

@AndroidEntryPoint
class SensorReportInformationListFragment : Fragment() {

    @Inject
    lateinit var sensorReportAdapter: SensorReportAdapter

    private val sensorReportViewModel: SensorReportViewModel by viewModels()

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
        }
    }

    private fun loadSensorReports() {
        sensorReportViewModel.publicReports.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> sensorReportAdapter.submitList(it.data)
                is ErrorResult -> Toast.makeText(activity, it.exception.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}