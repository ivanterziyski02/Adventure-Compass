package com.example.adventurecompass.chats;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.adventurecompass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button sendButton;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messageList;
    private DatabaseReference messagesRef;
    private String currentUserId;
    private String otherUserId;
    private String chatId;
    private ChatManager chatManager;
    private ImageView imageOtherProfile;
    private TextView textOtherName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_messages);
        inputMessage = findViewById(R.id.edit_message);
        sendButton = findViewById(R.id.button_send);
        imageOtherProfile = findViewById(R.id.image_other_profile);
        textOtherName = findViewById(R.id.text_other_name);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("userId");

        chatId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;

        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);
        messageList = new ArrayList<>();
        chatManager = new ChatManager();

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        checkBlockStatus();
    }

    private void checkBlockStatus() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(currentUserId).child("blocked").child(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        boolean iBlockedThem = snapshot1.exists();

                        usersRef.child(otherUserId).child("blocked").child(currentUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                        boolean theyBlockedMe = snapshot2.exists();
                                        boolean isBlocked = iBlockedThem || theyBlockedMe;

                                        loadMessages(isBlocked);

                                        if (isBlocked) {
                                            disableChatUI("Blocked user");
                                        } else {
                                            checkFriendshipStatus();
                                        }
                                    }

                                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkFriendshipStatus() {
        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("friends")
                .child(otherUserId);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean areFriends = snapshot.exists();

                if (!areFriends) {
                    disableChatUI("Not friends");
                } else {
                    enableChatUI();
                    loadUserInfo();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void disableChatUI(String reason) {
        inputMessage.setEnabled(false);
        sendButton.setEnabled(false);
        inputMessage.setHint(reason);
        textOtherName.setText("Unavailable");
        imageOtherProfile.setImageResource(R.drawable.ic_person);
    }

    private void enableChatUI() {
        sendButton.setEnabled(true);
        inputMessage.setEnabled(true);

        sendButton.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                chatManager.sendMessage(currentUserId, otherUserId, text);
                inputMessage.setText("");
            }
        });
    }

    private void loadMessages(boolean isBlocked) {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnap : snapshot.getChildren()) {
                    MessageModel message = messageSnap.getValue(MessageModel.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.setBlocked(isBlocked);
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUserInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(otherUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                if (name != null) textOtherName.setText(name);
                if (profileUrl != null && !profileUrl.isEmpty()) {
                    Glide.with(ChatActivity.this)
                            .load(profileUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(imageOtherProfile);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
