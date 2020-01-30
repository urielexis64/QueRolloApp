package com.example.querolloapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.querolloapp.R;
import com.example.querolloapp.activities.UserProfileImageActivity;
import com.example.querolloapp.entities.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, messagesRef;

    private int seleccionados = 0;
    private String lastDate;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
        messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        usersRef =  FirebaseDatabase.getInstance().getReference().child("Users");
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public CardView receiverCard, senderCard;
        public TextView senderMessageText, receiverMessageText, senderTime, receiverTime, txtDate;
        public LinearLayout mainLayout;
        public ImageView check, messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            senderTime = itemView.findViewById(R.id.sender_time);
            receiverTime = itemView.findViewById(R.id.receiver_time);
            receiverCard = itemView.findViewById(R.id.card1);
            senderCard = itemView.findViewById(R.id.card2);
            mainLayout = itemView.findViewById(R.id.main_layout);
            check = itemView.findViewById(R.id.check);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_picture);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_picture);
            txtDate = itemView.findViewById(R.id.txt_date);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        messageViewHolder.mainLayout.setTag("notselected");

        messageViewHolder.mainLayout.setOnLongClickListener(v -> {
            messageViewHolder.senderCard.setCardBackgroundColor(Color.parseColor("#6500E5FF"));
            messageViewHolder.receiverCard.setCardBackgroundColor(Color.parseColor("#6500E5FF"));
            messageViewHolder.mainLayout.setBackgroundColor(Color.parseColor("#6500E5FF"));
            messageViewHolder.mainLayout.setTag("selected");
            seleccionados++;
            CharSequence options[] = new CharSequence[]{
                    "Remove message", "Cancel"
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.mainLayout.getContext());
            builder.setTitle("Choose an option:");
            builder.setItems(options, (dialog, index) -> {
                if (index == 0)
                    removeMessage(i, messageViewHolder.itemView.getContext());
            });
            builder.show();

            return true;
        });

        messageViewHolder.mainLayout.setOnClickListener(v -> {
            if (seleccionados > 0 && !messageViewHolder.mainLayout.getTag().equals("selected")) {
                messageViewHolder.senderCard.setCardBackgroundColor(Color.parseColor("#6500E5FF"));
                messageViewHolder.receiverCard.setCardBackgroundColor(Color.parseColor("#6500E5FF"));
                messageViewHolder.mainLayout.setBackgroundColor(Color.parseColor("#6500E5FF"));
                messageViewHolder.mainLayout.setTag("selected");
                seleccionados++;
            } else if (seleccionados > 0) {
                messageViewHolder.senderCard.setCardBackgroundColor(ContextCompat.getColor(messageViewHolder.mainLayout.getContext(), R.color.chat_user_background));
                messageViewHolder.receiverCard.setCardBackgroundColor(Color.WHITE);
                messageViewHolder.mainLayout.setBackgroundColor(0);
                messageViewHolder.mainLayout.setTag("notselected");
                seleccionados--;
            }
        });

        messageViewHolder.receiverCard.setVisibility(View.GONE);
        messageViewHolder.senderCard.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);

        if (!messages.getDate().equals(lastDate)) {
            lastDate = messages.getDate();
            messageViewHolder.txtDate.setVisibility(View.VISIBLE);
            messageViewHolder.txtDate.setText(messages.getDate());
        }

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.mainLayout.setGravity(Gravity.END);
                messageViewHolder.senderCard.setVisibility(View.VISIBLE);
                messageViewHolder.check.setImageResource(R.drawable.ic_double_check);

                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderTime.setText(messages.getTime());
            } else {
                messageViewHolder.mainLayout.setGravity(Gravity.START);
                messageViewHolder.receiverCard.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverTime.setVisibility(View.GONE);
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.mainLayout.setGravity(Gravity.END);
                messageViewHolder.senderCard.setVisibility(View.VISIBLE);
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.check.setImageResource(R.drawable.ic_double_check);

                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                messageViewHolder.messageSenderPicture.setOnClickListener(v -> {
                    Context context = messageViewHolder.itemView.getContext();
                    Intent openImage = new Intent(context, UserProfileImageActivity.class);
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) messageViewHolder.messageSenderPicture.getDrawable());
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    openImage.putExtra("uri", imageInByte);
                    openImage.putExtra("username", "Tú");
                    context.startActivity(openImage);
                });
                messageViewHolder.senderTime.setText(messages.getTime());
            } else {
                messageViewHolder.mainLayout.setGravity(Gravity.START);
                messageViewHolder.receiverCard.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
                messageViewHolder.messageReceiverPicture.setOnClickListener(v -> {
                    Context context = messageViewHolder.itemView.getContext();
                    Intent openImage = new Intent(context, UserProfileImageActivity.class);
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) messageViewHolder.messageReceiverPicture.getDrawable());
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageInByte = stream.toByteArray();
                    openImage.putExtra("uri", imageInByte);

                    context.startActivity(openImage);
                });
                messageViewHolder.receiverTime.setText(messages.getTime());
            }
        }
    }

    private void removeMessage(int i, Context c) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserID = messages.getTo();
        String messageId = messages.getMessageID();

        messagesRef.child(messageSenderId).child(fromUserID).child(messageId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messagesRef.child(fromUserID).child(messageSenderId).child(messageId).removeValue().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Toast.makeText(c, c.getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}