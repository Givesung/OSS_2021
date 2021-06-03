package com.example.mainactivity.Matches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mainactivity.R;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesViewHolder> {
    private List<MatchesObject> matchesList;
    private Context context;


    public MatchesAdapter(List<MatchesObject> matchesList, Context context) {
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MatchesViewHolder rcv = new MatchesViewHolder((layoutView));

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolder holder, int position) {
        holder.mMatchId.setText(matchesList.get(position).getUserId());
        holder.mMatchName.setText(matchesList.get(position).getName());
        Glide.with(context).load(matchesList.get(position).getProfileImageUrl()).into(holder.mMatchImage);
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
