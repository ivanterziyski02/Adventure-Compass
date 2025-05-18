package com.example.adventurecompass.chats;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adventurecompass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ChatItemModel> chatList;
    private ChatListAdapter adapter;
    private DatabaseReference userChatsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recycler_chat_list);
        chatList = new ArrayList<>();
        adapter = new ChatListAdapter(chatList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(currentUserId);

        loadChats();

    }

    private void loadChats() {
        userChatsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    ChatItemModel chatItem = chatSnap.getValue(ChatItemModel.class);
                    if (chatItem != null) {
                        chatList.add(chatItem);
                    }
                }
                Collections.reverse(chatList); // за най-новите чатове най-отгоре
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}