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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(otherUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                if (name != null) {
                    textOtherName.setText(name);
                }

                if (profileUrl != null && !profileUrl.isEmpty()) {
                    Glide.with(ChatActivity.this)
                            .load(profileUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(imageOtherProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: log or show error
            }
        });


        chatId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;

        messageList = new ArrayList<>();
        chatManager = new ChatManager();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        loadMessages();

        sendButton.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                chatManager.sendMessage(currentUserId, otherUserId, text);
                inputMessage.setText("");
            }
        });
    }

    private void loadMessages() {
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
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void sendMessage(String text) {
        long timestamp = System.currentTimeMillis();
        MessageModel message = new MessageModel(currentUserId, otherUserId, text, timestamp);
        messagesRef.push().setValue(message);
        inputMessage.setText("");
    }
}