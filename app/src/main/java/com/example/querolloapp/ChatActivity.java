package com.example.querolloapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private EditText txtMessage;
    private ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverId = getIntent().getStringExtra("visit_user_id");
        messageReceiverName = getIntent().getStringExtra("visit_user_name");
        messageReceiverImage = getIntent().getStringExtra("visit_user_image");

        initializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
    }

    private void initializeControllers() {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userImage = findViewById(R.id.custom_profile_image);

        txtMessage = findViewById(R.id.input_message);
        btnSend= findViewById(R.id.send_message_button);

        txtMessage.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtMessage.getText().toString().trim().equals("")) {
                    btnSend.setImageResource(R.drawable.ic_mic);
                }else{
                    btnSend.setImageResource(R.drawable.ic_send);
                }
            }
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.layout).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.emoji).setTag(false);
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
}
