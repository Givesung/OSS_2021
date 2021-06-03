package com.example.mainactivity.Matches;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainactivity.R;

public class MatchesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mMatchId, mMatchName;
    public ImageView mMatchImage;

    public MatchesViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = itemView.findViewById(R.id.Matchid);
        mMatchName = itemView.findViewById(R.id.MatchName);
        mMatchImage = itemView.findViewById(R.id.MatchImage);
    }

    @Override
    public void onClick(View v) {

    }
}
