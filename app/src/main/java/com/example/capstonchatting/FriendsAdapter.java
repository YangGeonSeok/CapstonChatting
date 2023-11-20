package com.example.capstonchatting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>{
    private ArrayList<String> friendsList;

    public FriendsAdapter(ArrayList<String> friendsList) {
        this.friendsList = friendsList;
    }

    @Override
    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        holder.tvFriendName.setText(friendsList.get(position));
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        public FriendsViewHolder(View itemView) {
            super(itemView);

            tvFriendName = itemView.findViewById(R.id.frienditem_textview);
        }
    }
}