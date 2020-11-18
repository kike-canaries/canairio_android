package hpsaturn.pollutionreporter.report.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.report.domain.entities.SensorReportInformation
import kotlinx.android.synthetic.main.item_record.view.*
import javax.inject.Inject

class SensorReportAdapter @Inject constructor() :
    ListAdapter<SensorReportInformation, SensorReportViewHolder>(SensorReportDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorReportViewHolder =
        SensorReportViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        )

    override fun onBindViewHolder(holder: SensorReportViewHolder, position: Int) =
        holder.bind(getItem(position))
}

class SensorReportViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    fun bind(sensorReportInformation: SensorReportInformation) {
        itemView.tv_record_name.text = sensorReportInformation.deviceId
        itemView.tv_record_date.text = sensorReportInformation.date
        itemView.tv_record_location.text = "${sensorReportInformation.numberOfPoints}"
    }
}

private object SensorReportDiff : DiffUtil.ItemCallback<SensorReportInformation>() {
    override fun areItemsTheSame(
        oldItem: SensorReportInformation,
        newItem: SensorReportInformation
    ): Boolean = oldItem.deviceId == newItem.deviceId &&
            oldItem.date == newItem.date &&
            oldItem.lastLatitude == newItem.lastLatitude &&
            oldItem.lastLongitude == newItem.lastLongitude &&
            oldItem.numberOfPoints == newItem.numberOfPoints &&
            oldItem.name == newItem.name

    override fun areContentsTheSame(
        oldItem: SensorReportInformation,
        newItem: SensorReportInformation
    ): Boolean = oldItem == newItem
}