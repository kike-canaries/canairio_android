package hpsaturn.pollutionreporter.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hpsaturn.tools.Logger;

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

    private boolean showingData;
    private DatabaseReference mDatabase;
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
        mRecordsList.setHasFixedSize(true);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecordsList.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = mDatabase.child("tracks_info");

        Logger.d(TAG,"Query: "+postsQuery.toString());

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
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position, @NonNull SensorTrackInfo model) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.i(TAG,"onClick");

                    }
                });
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };

        mRecordsList.setAdapter(mAdapter);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private void updateUI() {
        if(mAdapter.getItemCount()>0) {
            mEmptyMessage.setVisibility(View.GONE);
            mRecordsList.setVisibility(View.VISIBLE);
        }else{
            mRecordsList.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Logger.d(TAG, "OnItemClickListener => Clicked: " + position + ", index " + mRecordsList.indexOfChild(view));
//            String recordId = mRecordsAdapter.getItem(position).name;
//            Logger.i(TAG, "showing record: "+recordId);
//            chart = ChartFragment.newInstance(recordId);
//            getMain().addFragmentPopup(chart,ChartFragment.TAG);
//            showingData=true;
        }
    };

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
