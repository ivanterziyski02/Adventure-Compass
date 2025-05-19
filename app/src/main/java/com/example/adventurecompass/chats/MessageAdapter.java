package com.example.adventurecompass.chats;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.adventurecompass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<MessageModel> messageList;
    private final String currentUserId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public MessageAdapter(List<MessageModel> messageList) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        String time = DateFormat.format("HH:mm", message.getTimestamp()).toString();

        boolean isLastFromUser = true;
        if (position + 1 < messageList.size()) {
            MessageModel nextMessage = messageList.get(position + 1);
            if (nextMessage.getSenderId().equals(message.getSenderId())) {
                isLastFromUser = false;
            }
        }

        if (holder instanceof SentViewHolder) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.textMessage.setText(message.getText());
            sentHolder.textTime.setText(time);
            sentHolder.textSenderName.setText("Me");
            sentHolder.imageProfile.setVisibility(isLastFromUser ? View.VISIBLE : View.INVISIBLE);

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(currentUserId);

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(sentHolder.imageProfile.getContext())
                                .load(profileUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(sentHolder.imageProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });


        } else {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.textMessage.setText(message.getText());
            receivedHolder.textTime.setText(time);

            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(message.getSenderId());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                    receivedHolder.textSenderName.setText(name != null ? name : "Unknown");
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(receivedHolder.imageProfile.getContext())
                                .load(profileUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(receivedHolder.imageProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            receivedHolder.imageProfile.setVisibility(isLastFromUser ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textMessage, textTime, textSenderName;

        SentViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            imageProfile = itemView.findViewById(R.id.image_profile);
            textTime = itemView.findViewById(R.id.text_time);
            textSenderName = itemView.findViewById(R.id.text_sender_name);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textMessage, textTime, textSenderName;

        ReceivedViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            imageProfile = itemView.findViewById(R.id.image_profile);
            textTime = itemView.findViewById(R.id.text_time);
            textSenderName = itemView.findViewById(R.id.text_sender_name);
        }
    }

}
