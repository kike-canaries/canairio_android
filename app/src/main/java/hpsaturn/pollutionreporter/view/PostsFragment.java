package hpsaturn.pollutionreporter.view;


import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.hpsaturn.tools.Logger;

import java.util.TimerTask;

import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorTrackInfo;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class PostsFragment extends Fragment {

    public static String TAG = PostsFragment.class.getSimpleName();

    private RecyclerView mRecordsList;
    private TextView mEmptyMessage;
    private ChartFragment chart;

    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<SensorTrackInfo, PostsViewHolder> mAdapter;

    public static PostsFragment newInstance() {
        PostsFragment fragment = new PostsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_records, container, false);
        mEmptyMessage = view.findViewById(R.id.tv_records_empty_list);
        mRecordsList = view.findViewById(R.id.rv_records);
        mEmptyMessage.setText(R.string.msg_not_public_recors);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecordsList.setLayoutManager(mManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getMain().getDatabase().child(Config.FB_TRACKS_INFO).orderByKey().limitToLast(20);
        Logger.d(TAG,"[FB][POSTS] Query: "+postsQuery.toString());
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<SensorTrackInfo>()
                .setQuery(postsQuery, SensorTrackInfo.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<SensorTrackInfo, PostsViewHolder>(options){

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new PostsViewHolder(inflater.inflate(R.layout.item_record, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position, @NonNull SensorTrackInfo trackInfo) {
                final DatabaseReference postRef = getRef(position);
                final String recordKey = postRef.getKey();
                Logger.d(TAG,"[FB][POSTS] onBindViewHolder: "+recordKey+" name:"+trackInfo.getName());
                getMain().addTrackToMap(trackInfo);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String recordId = trackInfo.getName();
                        Logger.i(TAG,"[FB][POSTS] onClick -> showing record: "+recordId);
                        chart = ChartFragment.newInstance(recordId);
                        getMain().addFragmentPopup(chart,ChartFragment.TAG);
                    }
                });
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(trackInfo);
            }
        };

        mRecordsList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mAdapter.startListening();
        mUpdateTimeTask.run(); // TODO: fucking workaround, firebase recycler wasn't update in fist time

    }

    private Handler mHandler = new Handler();
    private UpdateTimeTask mUpdateTimeTask = new UpdateTimeTask();

    class UpdateTimeTask extends TimerTask {
        private int retries = 5;
        private int counter = 0;
        public void run() {
            Logger.i(TAG,"[FB][POST] UpdateTimeTask, force refresh data..");
            refresh();
            if(counter++>retries)this.cancel();
            else mHandler.postDelayed(this,3000);
        }
    }

    private void updateUI() {
        if(mAdapter!=null&&mAdapter.getItemCount()>0) {
            mEmptyMessage.setVisibility(View.GONE);
            mRecordsList.setVisibility(View.VISIBLE);
        }else{
            mRecordsList.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    public void refresh() {
        if(mAdapter!=null){
            Logger.i(TAG,"[FB][POSTS] refresh query");
            mAdapter.startListening();
            updateUI();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }


}
