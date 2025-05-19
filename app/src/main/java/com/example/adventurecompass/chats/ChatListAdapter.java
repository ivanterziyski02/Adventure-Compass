package com.example.adventurecompass.chats;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.adventurecompass.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private final List<ChatItemModel> chatList;
    private final Context context;

    public ChatListAdapter(List<ChatItemModel> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItemModel item = chatList.get(position);
        String time = DateUtils.getRelativeTimeSpanString(
                item.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();


        holder.textLastMessage.setText(item.getLastMessage());
        holder.textTime.setText(time);

        // Зареждане на име и снимка от Firebase
        FirebaseDatabase.getInstance().getReference("users").child(item.getWith())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                        holder.textName.setText(name != null ? name : "Unknown");
                        if (profileUrl != null) {
                            Glide.with(context).load(profileUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(holder.imageProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", item.getWith());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textLastMessage, textTime;
        ImageView imageProfile;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textLastMessage = itemView.findViewById(R.id.text_last_message);
            textTime = itemView.findViewById(R.id.text_time);
            imageProfile = itemView.findViewById(R.id.image_profile);
        }
    }
}
