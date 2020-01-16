package com.example.querolloapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference chatRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList = requestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestsRef.child(currentUserId), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.btn_request_accept).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.btn_request_cancel).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received")) {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(requestUserStatus);
                                                holder.userStatus.setSelected(true);

                                                holder.itemView.setOnClickListener(v -> {
                                                    CharSequence options[] = new CharSequence[]{
                                                            getString(R.string.accept), getString(R.string.decline)
                                                    };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(requestUserName +" "+ getString(R.string.chat_request));
                                                    builder.setItems(options, (dialog, i) -> {
                                                        if (i == 0) {
                                                            contactsRef.child(currentUserId).child(list_user_id).child("Contact").setValue("Saved").addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    contactsRef.child(list_user_id).child(currentUserId).child("Contact").setValue("Saved").addOnCompleteListener(task2 -> {
                                                                        if (task2.isSuccessful()) {
                                                                            chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task3 -> {
                                                                                        if(task3.isSuccessful()){
                                                                                            chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(task4 -> {
                                                                                                        if(task4.isSuccessful()){
                                                                                                            Snackbar.make(getView(), "Contacto agregado con éxito", Snackbar.LENGTH_LONG).show();
                                                                                                        }

                                                                                                    });

                                                                                        }

                                                                                    });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        } else {
                                                            chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(task3 -> {
                                                                        if(task3.isSuccessful()){
                                                                            chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task4 -> {
                                                                                        if(task4.isSuccessful()){
                                                                                            Snackbar.make(getView(), "Contacto rechazado con éxito", Snackbar.LENGTH_LONG).show();
                                                                                        }

                                                                                    });

                                                                        }

                                                                    });

                                                        }
                                                    });
                                                    builder.show();
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_users_layout, parent, false);
                        return new RequestsViewHolder(view);
                    }
                };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button btnAccept, btnCancel;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            btnAccept = itemView.findViewById(R.id.btn_request_accept);
            btnCancel = itemView.findViewById(R.id.btn_request_cancel);
        }
    }

}
