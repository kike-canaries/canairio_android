package hpsaturn.pollutionreporter.view;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hpsaturn.pollutionreporter.R;


/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class RecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected final TextView record_date;
    protected final TextView record_location;
    protected final TextView record_name;
    private final ListRecordsAdapter mAdapter;


    public RecordViewHolder(View itemView, ListRecordsAdapter adapter) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.mAdapter=adapter;
        record_name = itemView.findViewById(R.id.tv_record_name);
        record_date = itemView.findViewById(R.id.tv_record_date);
        record_location = itemView.findViewById(R.id.tv_record_location);
    }

    @Override
    public void onClick(View view) {
        mAdapter.onItemHolderClick(this);
    }
}
