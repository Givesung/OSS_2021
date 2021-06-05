package com.example.mainactivity.Choice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mainactivity.Chat.ChatActivity;
import com.example.mainactivity.R;

public class ChoiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mChoiceId, mChoiceName;
    public ImageView mChoiceImage;

    public ChoiceViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mChoiceId = itemView.findViewById(R.id.ChoiceId);
        mChoiceName = itemView.findViewById(R.id.ChoiceName);

        mChoiceImage = itemView.findViewById(R.id.LikesImage);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putString("mChoiceId", mChoiceId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}
