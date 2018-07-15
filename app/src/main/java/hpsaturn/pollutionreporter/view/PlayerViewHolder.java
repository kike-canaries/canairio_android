package hpsaturn.pollutionreporter.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hpsaturn.pollutionreporter.R;


/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class PlayerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected final TextView player_email;
    protected final TextView player_phone;
    protected final TextView player_name;
    private final ListPlayersAdapter mAdapter;


    public PlayerViewHolder(View itemView, ListPlayersAdapter adapter) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.mAdapter=adapter;
        player_name = (TextView) itemView.findViewById(R.id.tv_player_name);
        player_email = (TextView) itemView.findViewById(R.id.tv_player_email);
        player_phone = (TextView) itemView.findViewById(R.id.tv_player_phone);
    }

    @Override
    public void onClick(View view) {
        mAdapter.onItemHolderClick(this);
    }
}
