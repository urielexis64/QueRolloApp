package com.example.querolloapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.querolloapp.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter {
    private Context contexto;
    private ArrayList<String> listOfGroups;
    private RecyclerViewClickListener listener;

    public RecyclerViewAdapter(Context contexto, ArrayList<String> listOfGroups) {
        this.contexto = contexto;
        this.listOfGroups = listOfGroups;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(contexto).inflate(R.layout.itempersonallinear, parent, false);
        return new Holder(contentView, listener);
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

    public void setOnClickListener(RecyclerViewClickListener listener) {
        this.listener = listener;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        TextView groupName;
        ImageView img;

        public Holder(final View itemView, final RecyclerViewClickListener listener) {
            super(itemView);
            groupName= itemView.findViewById(R.id.group_name);
            img = itemView.findViewById(R.id.profile_group_image);

            itemView.setOnClickListener(v -> {
                if(listener != null)
                    listener.onRowClicked(getAdapterPosition());
            });

            img.setOnClickListener(v -> {
                if(listener != null){
                    listener.onViewClicked(v, getAdapterPosition());
                }
            });

        }
    }
}
