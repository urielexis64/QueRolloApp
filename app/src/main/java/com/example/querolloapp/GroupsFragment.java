package com.example.querolloapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment implements RecyclerViewClickListener {

    private View groupFragmentView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager manager;
    private ArrayList<String> listOfGroups = new ArrayList<>();

    private DatabaseReference groupRef;

    public GroupsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        initializeFields();
        retrieveAndDisplayGroups();

        return groupFragmentView;
    }

    private void retrieveAndDisplayGroups() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                listOfGroups.clear();
                listOfGroups.addAll(set);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initializeFields() {
        recyclerView = groupFragmentView.findViewById(R.id.recycler_view);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        adapter = new RecyclerViewAdapter(getContext(), listOfGroups);

        ((RecyclerViewAdapter) adapter).setOnClickListener(this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRowClicked(int position) {

        String currentGroupName = listOfGroups.get(position);

        Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);

        groupChatIntent.putExtra("group_name", currentGroupName);
        startActivity(groupChatIntent);
    }

    @Override
    public void onViewClicked(View v, int position) {
        if (v instanceof ImageView) {
            Intent intent = new Intent(getContext(), ProfileImagePreviewActivity.class);

            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, "transition_dialog").toBundle();

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) ((ImageView) v).getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            intent.putExtra("byteArray", imageInByte);
            intent.putExtra("group_name", listOfGroups.get(position));
            startActivity(intent, bundle);
        }
    }
}
