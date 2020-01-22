package com.example.querolloapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.querolloapp.adapters.MessageAdapter;
import com.example.querolloapp.animations.MyBounceInterpolator;
import com.example.querolloapp.R;
import com.example.querolloapp.entities.Messages;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderId;
    private boolean oneCharacter = true, swipeFinish = true;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private EditText txtMessage;
    private ImageButton btnSend;

    private Animation myAnim;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getStringExtra("visit_user_id");
        messageReceiverName = getIntent().getStringExtra("visit_user_name");
        messageReceiverImage = getIntent().getStringExtra("visit_user_image");

        initializeToolbar();
        initializeInputChat();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
    }

    private void initializeToolbar() {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userImage = findViewById(R.id.custom_profile_image);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    private void initializeInputChat() {
        myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce_animation);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.3, 5);
        myAnim.setInterpolator(interpolator);

        txtMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.send_message_button);
        findViewById(R.id.layout_back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.emoji).setTag(false);

        btnSend.setOnClickListener(v -> sendMessage());

        txtMessage.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = txtMessage.getText().toString();
                if (TextUtils.isEmpty(text.trim())) {
                    btnSend.setImageResource(R.drawable.ic_mic);
                    if (text.equals("") && !oneCharacter) {
                        btnSend.startAnimation(myAnim);
                        oneCharacter = true;
                    }
                } else {
                    btnSend.setImageResource(R.drawable.ic_send);
                    if (text.length() == 1 && oneCharacter) {
                        oneCharacter = false;
                        btnSend.startAnimation(myAnim);
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(ScrollView.FOCUS_DOWN);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String messageText = txtMessage.getText().toString().trim();
        if (messageText.length() == 0)
            return;
        String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
        String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

        DatabaseReference userMessagesKeyRef = rootRef.child("Messages")
                .child(messageSenderRef).child(messageReceiverId).push();
        String messagePushId = userMessagesKeyRef.getKey();
        Map messageTextBody = new HashMap();
        messageTextBody.put("message", messageText);
        messageTextBody.put("type", "text");
        messageTextBody.put("from", messageSenderId);
        messageTextBody.put("messageID", messagePushId);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Snackbar.make(txtMessage, "Mensaje enviado con éxito", Snackbar.LENGTH_SHORT).show();
            else
                Snackbar.make(txtMessage, "Error: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
            txtMessage.setText("");
        });
    }

    public void emojiAction(View view) {
        ImageButton btnEmoji = (ImageButton) view;
        if (!((boolean) btnEmoji.getTag())) {
            btnEmoji.setImageResource(R.drawable.ic_keyboard);
            btnEmoji.setTag(true);
        } else {
            btnEmoji.setImageResource(R.drawable.ic_emoji);
            btnEmoji.setTag(false);
        }
    }

    @Override
    public void onBackPressed() {
        swipeFinish = false;
        super.onBackPressed();
    }

    public void finish() {
        super.finish();
        if (swipeFinish)
            overridePendingTransition(0, 0);
    }
}
