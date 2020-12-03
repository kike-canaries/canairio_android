package hpsaturn.pollutionreporter.view;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import hpsaturn.pollutionreporter.R;


/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class RecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected final TextView record_date;
    protected final TextView record_location;
    protected final TextView record_name;
    private final RecordsAdapter mAdapter;


    public RecordViewHolder(View itemView, RecordsAdapter adapter) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.mAdapter = adapter;
        record_name = itemView.findViewById(R.id.stationName);
        record_date = itemView.findViewById(R.id.reportDate);
        record_location = itemView.findViewById(R.id.reportNumberOfPoints);
    }

    @Override
    public void onClick(View view) {
        mAdapter.onItemHolderClick(this);
    }
}
