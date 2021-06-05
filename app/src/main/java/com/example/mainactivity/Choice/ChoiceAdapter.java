package com.example.mainactivity.Choice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mainactivity.R;

import java.util.List;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceViewHolder> {
    private List<ChoiceObject> ChoiceList;
    private Context context;


    public ChoiceAdapter(List<ChoiceObject> ChoiceList, Context context) {
        this.ChoiceList = ChoiceList;
        this.context = context;
    }

    @Override
    public ChoiceViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choice, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        ChoiceViewHolder rcv = new ChoiceViewHolder((layoutView));

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceViewHolder holder, int position) {
        holder.mChoiceId.setText(ChoiceList.get(position).getUserId());
        holder.mChoiceName.setText(ChoiceList.get(position).getName());
        Glide.with(context).load(ChoiceList.get(position).getProfileImageUrl()).into(holder.mChoiceImage);
    }

    @Override
    public int getItemCount() {
        return this.ChoiceList.size();
    }
}
