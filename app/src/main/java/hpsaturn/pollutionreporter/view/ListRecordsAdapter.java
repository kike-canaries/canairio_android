package hpsaturn.pollutionreporter.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorTrack;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */

public class ListRecordsAdapter extends RecyclerView.Adapter<RecordViewHolder> implements ItemTouchHelperAdapter {

    private AdapterView.OnItemClickListener mOnItemClickListener;
    private Context ctx;
    private ArrayList<SensorTrack> mRecords =new ArrayList<>();

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View rowView = inflater.inflate(R.layout.item_record, parent, false);
        this.ctx=parent.getContext();
        return new RecordViewHolder(rowView,this);
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {

        final SensorTrack sensorTrack = mRecords.get(position);

        holder.record_name.setText(sensorTrack.getName());
        holder.record_date.setText(sensorTrack.getDate());
        // TODO: geocode inverse for location
        holder.record_location.setText("");

    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    public void updateData (ArrayList<SensorTrack>records){
        this.mRecords =records;
        notifyDataSetChanged();
    }

    public void addItem(int position, SensorTrack record) {
        if (position > mRecords.size()) return;
        mRecords.add(position, record);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        if (position >= mRecords.size()) return;
        mRecords.remove(position);
        notifyItemRemoved(position);
    }


    public void updateItem(int position, SensorTrack record) {
        if (position > mRecords.size()) return;
        mRecords.set(position, record);
        notifyItemChanged(position);
    }

    public SensorTrack getItem(int position) {
        return mRecords.get(position);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void onItemHolderClick(RecordViewHolder itemHolder) {

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }

    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mRecords, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mRecords, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mRecords.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemAction(int position, int action) {

    }

    public List<SensorTrack> getRecords() {
        return mRecords;
    }

}
