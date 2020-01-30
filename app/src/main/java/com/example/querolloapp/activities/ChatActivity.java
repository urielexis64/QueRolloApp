package com.example.querolloapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.querolloapp.adapters.MessageAdapter;
import com.example.querolloapp.animations.MyBounceInterpolator;
import com.example.querolloapp.R;
import com.example.querolloapp.entities.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.codetail.animation.ViewAnimationUtils;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderId;
    private String checker, myUrl;
    private boolean oneCharacter = true, swipeFinish = true, clicked = true;
    private Uri fileUri;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private EditText txtMessage;
    private ImageButton btnSend, btnSendFiles;
    private CardView chooseFileCard;

    private String saveCurrentTime, saveCurrentDate;

    private Animation buttonAnim;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageTask uploadTask;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getStringExtra("visit_user_id");
        messageReceiverName = getIntent().getStringExtra("visit_user_name");
        messageReceiverImage = getIntent().getStringExtra("visit_user_image");

        chooseFileCard = findViewById(R.id.choose_card);
        chooseFileCard.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                showChooseFileCard(false);
                Toast.makeText(this, "Perdí el foco", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "gane el foco", Toast.LENGTH_SHORT).show();

        });

        initializeToolbar();
        initializeInputChat();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        displayLastSeen();

        start();
    }

    private void start() {
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

    private void displayLastSeen() {
        rootRef.child("Users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals(getResources().getString(R.string.online))) {
                                userLastSeen.setText(getResources().getString(R.string.online));
                            } else if (state.equals(getResources().getString(R.string.offline))) {
                                userLastSeen.setText("últ. vez " + date + " a las " + time);
                            }
                        } else {
                            userLastSeen.setText(getResources().getString(R.string.offline));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initializeInputChat() {
        buttonAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce_animation);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.3, 5);
        buttonAnim.setInterpolator(interpolator);

        txtMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.send_message_button);
        btnSendFiles = findViewById(R.id.send_files);
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
                    if (text.trim().equals("") && !oneCharacter) {
                        btnSend.startAnimation(buttonAnim);
                        oneCharacter = true;
                        ObjectAnimator animation = ObjectAnimator.ofFloat(findViewById(R.id.right_buttons_layout), "translationX", -5f);
                        animation.setDuration(200);
                        animation.start();
                        rightButtonsAnimation(false);
                    }
                } else {
                    btnSend.setImageResource(R.drawable.ic_send);
                    if (text.trim().length() > 0 && oneCharacter) {
                        oneCharacter = false;
                        btnSend.startAnimation(buttonAnim);
                        ObjectAnimator animation = ObjectAnimator.ofFloat(findViewById(R.id.right_buttons_layout), "translationX", 65f);
                        animation.setDuration(200);
                        animation.start();
                        rightButtonsAnimation(true);
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd 'de' MMMM 'del' yyyy", new Locale("ES"));
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    private void rightButtonsAnimation(boolean yes) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5.6f);
        params.setMarginEnd(yes ? -60 : 0);
        txtMessage.setLayoutParams(params);
    }


    private void sendMessage() {
        String messageText = txtMessage.getText().toString().trim();
        if (messageText.length() == 0)
            return;
        txtMessage.setText("");
        String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
        String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

        DatabaseReference userMessagesKeyRef = rootRef.child("Messages")
                .child(messageSenderRef).child(messageReceiverId).push();
        String messagePushId = userMessagesKeyRef.getKey();
        Map<String, String> messageTextBody = new HashMap<>();
        messageTextBody.put("message", messageText);
        messageTextBody.put("type", "text");
        messageTextBody.put("from", messageSenderId);
        messageTextBody.put("to", messageReceiverId);
        messageTextBody.put("messageID", messagePushId);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);

        Map<String, Object> messageBodyDetails = new HashMap<>();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

        rootRef.updateChildren(messageBodyDetails);
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

    Animator animator;


    private void showChooseFileCard(boolean show) {
        int cx = chooseFileCard.getRight() - 200;
        int cy = chooseFileCard.getBottom();
        int dx = Math.max(cx, chooseFileCard.getWidth() - cx);
        int dy = Math.max(cy, chooseFileCard.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        if (show) {
            animator =
                    ViewAnimationUtils.createCircularReveal(chooseFileCard, cx, cy, 0, finalRadius);
            chooseFileCard.setVisibility(View.VISIBLE);
        } else {
            animator =
                    ViewAnimationUtils.createCircularReveal(chooseFileCard, cx, cy, finalRadius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    chooseFileCard.setVisibility(View.INVISIBLE);
                }
            });
        }
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    public void showChooseFile(View view) {
        showChooseFileCard(clicked);
        clicked = !clicked;
    }

    public void fileImage(View view) {
        checker = "image";
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i.createChooser(i, "Selecciona una imagen..."), 438);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            loadingBar.setTitle("Enviando");
            loadingBar.setMessage("Por favor, espere...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            switch (checker) {
                case "image":
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                    String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                    String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                    DatabaseReference userMessagesKeyRef = rootRef.child("Messages")
                            .child(messageSenderRef).child(messageReceiverId).push();
                    final String messagePushId = userMessagesKeyRef.getKey();

                    StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                    uploadTask = filePath.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUrl = task.getResult();
                                myUrl = downloadUrl.toString();

                                Map<String, String> messageTextBody = new HashMap<>();
                                messageTextBody.put("message", myUrl);
                                messageTextBody.put("name", fileUri.getLastPathSegment());
                                messageTextBody.put("type", checker);
                                messageTextBody.put("from", messageSenderId);
                                messageTextBody.put("to", messageReceiverId);
                                messageTextBody.put("messageID", messagePushId);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);

                                Map<String, Object> messageBodyDetails = new HashMap<>();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task1 -> {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Archivo enviado", Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(ChatActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });

                    break;
                case "audio":
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + checker);
            }

        }
    }
}
