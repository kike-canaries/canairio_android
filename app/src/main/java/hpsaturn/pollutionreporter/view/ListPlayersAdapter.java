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
import hpsaturn.pollutionreporter.models.PlayerItem;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */

public class ListPlayersAdapter extends RecyclerView.Adapter<PlayerViewHolder> implements ItemTouchHelperAdapter {

    private AdapterView.OnItemClickListener mOnItemClickListener;
    private Context ctx;
    private ArrayList<PlayerItem>mPlayers=new ArrayList<>();

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View rowView = inflater.inflate(R.layout.item_player_list, parent, false);
        this.ctx=parent.getContext();
        return new PlayerViewHolder(rowView,this);
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder holder, int position) {

        final PlayerItem player = mPlayers.get(position);

        holder.player_name.setText(player.getName());
        holder.player_email.setText(player.getEmail());
        holder.player_phone.setText(player.getPhone());

    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    public void updateData (ArrayList<PlayerItem>players){
        this.mPlayers=players;
        notifyDataSetChanged();
    }

    public void addItem(int position, PlayerItem player) {
        if (position > mPlayers.size()) return;
        mPlayers.add(position, player);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        if (position >= mPlayers.size()) return;
        mPlayers.remove(position);
        notifyItemRemoved(position);
    }


    public void updateItem(int position, PlayerItem player) {
        if (position > mPlayers.size()) return;
        mPlayers.set(position, player);
        notifyItemChanged(position);
    }

    public PlayerItem getItem(int position) {
        return mPlayers.get(position);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void onItemHolderClick(PlayerViewHolder itemHolder) {

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }

    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mPlayers, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mPlayers, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mPlayers.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemAction(int position, int action) {

    }

    public List<PlayerItem> getPlayers() {
        return mPlayers;
    }

}
