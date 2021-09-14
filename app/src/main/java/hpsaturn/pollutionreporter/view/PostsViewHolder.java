package hpsaturn.pollutionreporter.view;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hpsaturn.pollutionreporter.R;
import com.jetbrains.handson.commons.models.SensorTrackInfo;


/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class PostsViewHolder extends RecyclerView.ViewHolder {

    public TextView record_date;
    public TextView record_location;
    public TextView record_name;

    public PostsViewHolder(View itemView) {
        super(itemView);

        record_name = itemView.findViewById(R.id.tv_record_name);
        record_date = itemView.findViewById(R.id.tv_record_date);
        record_location = itemView.findViewById(R.id.tv_record_location);
    }

    public void bindToPost(SensorTrackInfo sensorTrack){
        record_name.setText(sensorTrack.getName());
        record_date.setText(sensorTrack.getDate());
        // TODO: geocode inverse for location
        record_location.setText(""+sensorTrack.getSize()+" points");
    }

}
