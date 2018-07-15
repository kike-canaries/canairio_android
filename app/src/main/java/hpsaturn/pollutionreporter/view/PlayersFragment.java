package hpsaturn.pollutionreporter.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.PlayerItem;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class PlayersFragment extends Fragment {

    private static final boolean DEBUG = Config.DEBUG;
    public static String TAG = PlayersFragment.class.getSimpleName();

    private RecyclerView mPlayersRecycler;
    private ListPlayersAdapter mPlayersAdapter;
    private TextView mEmptyMessage;

    public static PlayersFragment newInstance() {
        PlayersFragment fragment = new PlayersFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.players_fragment, container, false);
        mEmptyMessage = (TextView)view.findViewById(R.id.tv_players_empty_list);
        mPlayersRecycler = (RecyclerView) view.findViewById(R.id.rv_players);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mPlayersRecycler.setLayoutManager(gridLayoutManager);

        mPlayersAdapter = new ListPlayersAdapter();
        mPlayersAdapter.setOnItemClickListener(onItemClickListener);
        mPlayersRecycler.setAdapter(mPlayersAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mPlayersAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mPlayersRecycler);

        return view;

    }

    public void addPlayer(PlayerItem player){
        mPlayersAdapter.addItem(0, player);
        mPlayersRecycler.scrollToPosition(0);
//        Storage.addPlayer(getActivity(), player);
        updateUI();
    }

    public void updatePlayer(PlayerItem oldPlayer,PlayerItem newPlayer,int position) {
        mPlayersAdapter.updateItem(position, newPlayer);
//        Storage.updatePlayer(getActivity(), oldPlayer, newPlayer);
//        if(Storage.isGameStart(getActivity())){
//            Storage.updateSendData(getActivity(),oldPlayer,newPlayer);
//            getMain().getSendMessageFragment().notifyUpdatePlayer();
//        }
        updateUI();
    }

    private void updateUI() {
        if(mPlayersAdapter.getItemCount()>0) {
            mEmptyMessage.setVisibility(View.GONE);
            mPlayersRecycler.setVisibility(View.VISIBLE);
        }else{
            mPlayersRecycler.setVisibility(View.GONE);
            mEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    public int getPlayersCount(){
        return mPlayersAdapter.getItemCount();
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            if(DEBUG) Log.d(TAG, "OnItemClickListener => Clicked: " + position + ", index " + mPlayersRecycler.indexOfChild(view));
//            getMain().showAddDialog(mPlayersAdapter.getItem(position),position);
        }
    };

    public List<PlayerItem> getPlayers() {
        return mPlayersAdapter.getPlayers();
    }

    public void removePlayers() {
//        Storage.setPlayers(getActivity(),new ArrayList<PlayerItem>());
//        mPlayersAdapter.updateData(Storage.getPlayers(getActivity()));
    }


    public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
//            if(Storage.isGameStart(getActivity()))return false;
//            else return true;
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
//            if(Storage.isGameStart(getActivity()))return false;
//            else return true;
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);

        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            if (DEBUG) Log.d(TAG, "ItemTouchHelperCallback: onMove");
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
