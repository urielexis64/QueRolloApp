package com.example.querolloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText txtMessage;
    private FloatingActionButton btnSendMessage;
    private ScrollView scrollView;
    private LinearLayout parentLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;

    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;
    private boolean swipeFinish = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getStringExtra("group_name");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        initializeFields();
        getUserInfo();

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        btnSendMessage.setOnClickListener(v -> {
            saveMessageInfoToDataBase();
            txtMessage.setText("");
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot);
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                }
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

    private void initializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        mToolbar.setTitle(currentGroupName);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtMessage = findViewById(R.id.input_group_message);
        btnSendMessage = findViewById(R.id.send_message_button);
        scrollView = findViewById(R.id.my_scroll_view);
        parentLayout = findViewById(R.id.layout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        swipeFinish = false;
        finish();
    }

    public void finish() {
        super.finish();
        if (swipeFinish)
            overridePendingTransition(0, 0);
    }

    private void getUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageInfoToDataBase() {
        String message = txtMessage.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message))
            return;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd 'de' MMMM 'del' yyyy", new Locale("ES"));
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();
        groupNameRef.updateChildren(groupMessageKey);

        groupMessageKeyRef = groupNameRef.child(messageKey);

        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currentUserName);
        messageInfoMap.put("message", message);
        messageInfoMap.put("date", currentDate);
        messageInfoMap.put("time", currentTime);

        groupMessageKeyRef.updateChildren(messageInfoMap);
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            parentLayout.addView(addCardMessage(chatDate, chatName, chatMessage, chatTime));
            lastDate = chatDate;
        }
    }

    private String lastDate;

    private CardView addCardMessage(String date, String username, String message, String time) {
        if (date == null || !date.equals(lastDate)) {
            TextView txtDate = new TextView(this);
            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewParams.gravity = Gravity.CENTER;
            viewParams.setMargins(0, 10, 0, 10);
            txtDate.setLayoutParams(viewParams);
            txtDate.setPadding(10, 10, 10, 10);
            txtDate.setTextSize(14);
            txtDate.setAllCaps(true);
            txtDate.setBackground(ContextCompat.getDrawable(this, R.drawable.background_group_date_text));
            txtDate.setText(date);
            parentLayout.addView(txtDate);
        }

        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardViewParams.setMargins(15, 0, 0, 20);
        cardView.setLayoutParams(cardViewParams);
        cardView.setRadius(15f);
        cardView.setCardElevation(5f);

        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setLayoutParams(cardViewParams);
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView txtUsername = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(4, 0, 4, 0);
        txtUsername.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        txtUsername.setTextSize(12f);
        txtUsername.setTypeface(Typeface.DEFAULT_BOLD);
        txtUsername.setText(username);
        txtUsername.setLayoutParams(textViewParams);
        int r = (int) (Math.random() * 255);
        int g = (int) (Math.random() * 255);
        int b = (int) (Math.random() * 255);
        txtUsername.setTextColor(Color.rgb(r, g, b));

        LinearLayout secondaryLinearLayout = new LinearLayout(this);
        LinearLayout.MarginLayoutParams secondaryMarginParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secondaryLinearLayout.setLayoutParams(secondaryMarginParams);
        secondaryLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView txtMessage = new TextView(this);
        txtMessage.setText(message);
        txtMessage.setPadding(0, 2, 2, 2);
        txtMessage.setMaxWidth(350);
        txtMessage.setTextSize(14f);
        txtMessage.setTextColor(ContextCompat.getColor(this, android.R.color.background_dark));
        txtMessage.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        txtMessage.setSingleLine(false);
        LinearLayout.MarginLayoutParams txtMarginParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtMarginParams.setMargins(4, 0, 20, 0);
        txtMessage.setLayoutParams(txtMarginParams);

        TextView txtMessageTime = new TextView(this);
        LinearLayout.MarginLayoutParams txtTimeMarginParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        txtMessageTime.setPadding(2, 4, 4, 0);
        txtMessageTime.setTextSize(11f);
        txtMessageTime.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        txtTimeMarginParams.setMarginStart(2);
        txtTimeMarginParams.setMarginEnd(2);
        txtMessageTime.setGravity(Gravity.BOTTOM);
        txtMessageTime.setText(time);
        txtMessageTime.setLayoutParams(txtTimeMarginParams);

        secondaryLinearLayout.addView(txtMessage);
        secondaryLinearLayout.addView(txtMessageTime);

        if (username.equals(currentUserName)) {
            cardView.setCardBackgroundColor(Color.parseColor("#D1FFCE"));
            cardViewParams.gravity = Gravity.END;
            cardViewParams.setMargins(0, 0, 15, 20);
        } else {
            mainLinearLayout.addView(txtUsername);
        }

        mainLinearLayout.addView(secondaryLinearLayout);

        cardView.addView(mainLinearLayout);

        return cardView;
    }

}
