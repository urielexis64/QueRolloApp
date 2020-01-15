package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, senderUserId, currentState;

    @BindView(R.id.visit_profile_image)
    CircleImageView userProfileImage;
    @BindView(R.id.visit_user_name)
    TextView userProfileName;
    @BindView(R.id.visit_profile_status)
    TextView userProfileStatus;
    @BindView(R.id.send_message_request_button)
    Button btnSendMessageRequest;
    @BindView(R.id.decline_message_request_button)
    Button btnDeclineRequest;

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth mAtuh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mAtuh = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        currentState = "new";
        senderUserId = mAtuh.getCurrentUser().getUid();

        receiverUserId = getIntent().getStringExtra("visit_user_id");
        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                }
                manageChatRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void manageChatRequests() {
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserId)) {
                    String requestType = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if (requestType.equals("sent")) {
                        currentState = "request_sent";
                        btnSendMessageRequest.setText("Cancelar solicitud");
                    } else if (requestType.equals("received")) {
                        currentState = "request_received";
                        btnSendMessageRequest.setText("Aceptar solicitud");

                        btnDeclineRequest.setVisibility(View.VISIBLE);
                        btnDeclineRequest.setEnabled(true);

                        btnDeclineRequest.setOnClickListener(v -> cancelChatRequest());
                    }
                } else {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserId)) {
                                currentState = "friends";
                                btnSendMessageRequest.setText("Eliminar contacto");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(receiverUserId)) {
            btnSendMessageRequest.setOnClickListener(v -> {
                btnSendMessageRequest.setEnabled(false);
                if (currentState.equals("new")) {
                    sendChatRequest();
                }
                if (currentState.equals("request_sent")) {
                    cancelChatRequest();
                }
                if (currentState.equals("request_received")) {
                    acceptChatRequest();
                }
                if (currentState.equals("friends")) {
                    removeSpecificContact();
                }

            });

        } else {
            btnSendMessageRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = "new";
                        btnSendMessageRequest.setText("Enviar mensaje");

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);
                    }
                });
            }
        });
    }

    private void acceptChatRequest() {

        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(task3 -> {
                                    btnSendMessageRequest.setEnabled(true);
                                    currentState = "friends";
                                    btnSendMessageRequest.setText("Eliminar contacto");
                                    btnDeclineRequest.setVisibility(View.INVISIBLE);
                                    btnDeclineRequest.setEnabled(false);
                                });
                            }
                        });
                    }
                });
            }
        });

    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        btnSendMessageRequest.setEnabled(true);
                        currentState = "new";
                        btnSendMessageRequest.setText("Enviar mensaje");

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);
                    }
                });
            }
        });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId)
                .child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatRequestRef.child(receiverUserId).child(senderUserId)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    btnSendMessageRequest.setEnabled(true);
                                    currentState = "request_sent";
                                    btnSendMessageRequest.setText("Cancelar solicitud");
                                });
                    }
                });
    }
}
