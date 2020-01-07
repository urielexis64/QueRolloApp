package com.example.querolloapp;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface RecyclerViewClickListener {

    void onRowClicked(int position);
    void onViewClicked(View v, int position);
}