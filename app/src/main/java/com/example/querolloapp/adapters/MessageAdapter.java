package com.example.querolloapp.adapters;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.querolloapp.R;
import com.example.querolloapp.entities.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, messagesRef;

    private int seleccionados = 0;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
        messagesRef= FirebaseDatabase.getInstance().getReference().child("Messages");
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public CardView receiverCard, senderCard;
        public TextView senderMessageText, receiverMessageText, senderTime, receiverTime;
        public RelativeLayout mainLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            senderTime = itemView.findViewById(R.id.sender_time);
            receiverTime = itemView.findViewById(R.id.receiver_time);
            receiverCard = itemView.findViewById(R.id.card1);
            senderCard = itemView.findViewById(R.id.card2);
            mainLayout = itemView.findViewById(R.id.main_layout);
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
                if(index == 0)
                    removeMessage(i);
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
            }else if(seleccionados > 0){
                messageViewHolder.senderCard.setCardBackgroundColor(ContextCompat.getColor(messageViewHolder.mainLayout.getContext(), R.color.chat_user_background));
                messageViewHolder.receiverCard.setCardBackgroundColor(Color.WHITE);
                messageViewHolder.mainLayout.setBackgroundColor(0);
                messageViewHolder.mainLayout.setTag("notselected");
                seleccionados--;
            }
        });

        if (fromMessageType.equals("text")) {
            messageViewHolder.receiverCard.setVisibility(View.INVISIBLE);
            messageViewHolder.senderCard.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.mainLayout.setGravity(Gravity.END);
                messageViewHolder.senderCard.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderTime.setText("10:00 a. m.");
            } else {
                messageViewHolder.mainLayout.setGravity(Gravity.START);
                messageViewHolder.receiverCard.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverTime.setText("10:00 a. m.");
            }
        }
    }

    private void removeMessage(int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserID = messages.getTo();
        String messageId = messages.getMessageID();

        System.out.println("Messages/"+messageSenderId+"/"+fromUserID+"/"+messageId);

        messagesRef.child(messageSenderId).child(fromUserID).child(messageId).removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                messagesRef.child(fromUserID).child(messageSenderId).child(messageId).removeValue().addOnCompleteListener(task2 -> {
                    if(task2.isSuccessful()){
                        System.out.println("Mensaje eliminado");
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