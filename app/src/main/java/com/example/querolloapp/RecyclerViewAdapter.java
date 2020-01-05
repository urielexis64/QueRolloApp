package com.example.querolloapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private Context contexto;
    private ArrayList<String> listOfGroups;
    private View.OnClickListener listener;

    public RecyclerViewAdapter(Context contexto, ArrayList<String> listOfGroups) {
        this.contexto = contexto;
        this.listOfGroups = listOfGroups;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(contexto).inflate(R.layout.itempersonallinear, null);
        contentView.setOnClickListener(this);
        return new Holder(contentView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String currentGroup = listOfGroups.get(position);
        Holder hold = (Holder) holder;
        hold.groupName.setText(currentGroup);
    }

    @Override
    public int getItemCount() {
        return listOfGroups.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    public static class Holder extends RecyclerView.ViewHolder {

        TextView groupName;

        public Holder(@NonNull View itemView) {
            super(itemView);
            groupName= itemView.findViewById(R.id.groupName);
        }
    }
}
