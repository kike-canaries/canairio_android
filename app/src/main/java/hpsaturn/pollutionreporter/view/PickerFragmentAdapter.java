package hpsaturn.pollutionreporter.view;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import hpsaturn.pollutionreporter.R;

/**
 * created by antonio vanegas @hpsaturn on 7/23/18.
 */

public class PickerFragmentAdapter extends RecyclerView.Adapter<PickerFragmentAdapter.ViewHolder> {

    private RecyclerView parentRecycler;
    private List<PickerFragmentInfo> data;

    public PickerFragmentAdapter(List<PickerFragmentInfo> data) {
        this.data = data;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecycler = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_fragment_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PickerFragmentInfo pickerFragmentInfo = data.get(position);
        Glide.with(holder.itemView.getContext())
                .load(pickerFragmentInfo.getIcon())
                .into(holder.imageView);
        holder.textView.setText(pickerFragmentInfo.getName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.fragment_image);
            textView = (TextView) itemView.findViewById(R.id.fragment_name);
            itemView.findViewById(R.id.container).setOnClickListener(this);
        }

        public void showText() {
            int parentHeight = ((View) imageView.getParent()).getHeight();
            float scale = (parentHeight - textView.getHeight()) / (float) imageView.getHeight();
            imageView.setPivotX(imageView.getWidth() * 0.5f);
            imageView.setPivotY(0);
            imageView.animate().scaleX(scale)
                    .withEndAction(() -> textView.setVisibility(View.VISIBLE))
                    .scaleY(scale).setDuration(200)
                    .start();
        }

        public void hideText() {
            textView.setVisibility(View.INVISIBLE);
            imageView.animate().scaleX(1f).scaleY(1f)
                    .setDuration(200)
                    .start();
        }

        @Override
        public void onClick(View v) {
            parentRecycler.smoothScrollToPosition(getAdapterPosition());
        }
    }

}
