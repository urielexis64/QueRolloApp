package com.example.querolloapp.adapters;

import android.view.View;

public interface RecyclerViewClickListener {

    void onRowClicked(int position);
    void onViewClicked(View v, int position);
}