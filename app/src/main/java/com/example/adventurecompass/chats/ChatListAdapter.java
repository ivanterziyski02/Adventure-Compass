package com.example.adventurecompass.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.adventurecompass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final List<ChatItemModel> chatList;
    private final Context context;
    private final String currentUserId;
    private final Set<String> blockedUserIds = new HashSet<>();

    public ChatListAdapter(List<ChatItemModel> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ChatItemModel item = chatList.get(position);
        String chatPartnerId = item.getWith();

        String time = DateUtils.getRelativeTimeSpanString(
                item.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
        holder.textTime.setText(time);
        holder.textLastMessage.setText(item.getLastMessage());

        if (blockedUserIds.contains(chatPartnerId)) {
            showBlockedUI(holder);
            return;
        }

        checkIfBlocked(currentUserId, chatPartnerId, isBlocked -> {
            if (isBlocked) {
                showBlockedUI(holder);
                blockedUserIds.add(chatPartnerId);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userId", chatPartnerId);
                    context.startActivity(intent);
                });
            } else {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(chatPartnerId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (holder.getAdapterPosition() != position) return;

                                String name = snapshot.child("name").getValue(String.class);
                                String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                                holder.textName.setText(name != null ? name : "Unknown");

                                if (profileUrl != null && !profileUrl.isEmpty()) {
                                    Glide.with(context).load(profileUrl)
                                            .placeholder(R.drawable.ic_profile_placeholder)
                                            .into(holder.imageProfile);
                                } else {
                                    holder.imageProfile.setImageResource(R.drawable.ic_profile_placeholder);
                                }

                                holder.itemView.setOnClickListener(v -> {
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("userId", chatPartnerId);
                                    context.startActivity(intent);
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
            }
        });
    }

    private void showBlockedUI(ChatViewHolder holder) {
        holder.textName.setText("Blocked user");
        holder.textLastMessage.setText("");
        holder.imageProfile.setImageResource(R.drawable.ic_person);
        holder.itemView.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void clearBlockedCache() {
        blockedUserIds.clear();
        notifyDataSetChanged();
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

    private interface BlockCheckCallback {
        void onResult(boolean isBlocked);
    }

    private void checkIfBlocked(String me, String other, BlockCheckCallback callback) {
        FirebaseDatabase.getInstance().getReference("users").child(me).child("blocked").child(other)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        boolean iBlockedThem = snapshot1.exists();

                        FirebaseDatabase.getInstance().getReference("users").child(other).child("blocked").child(me)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                        boolean theyBlockedMe = snapshot2.exists();
                                        callback.onResult(iBlockedThem || theyBlockedMe);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
