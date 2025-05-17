package com.example.adventurecompass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ReceivedRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TextView emptyText;
    private String currentUserId;
    private final List<UserModel> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_requests);

        recyclerView = findViewById(R.id.requestsRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userAdapter = new UserAdapter(this, requestList);
        recyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(user -> {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.orderByChild("email").equalTo(user.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String selectedUid = child.getKey();
                                if (selectedUid.equals(currentUserId)) {
                                    startActivity(new Intent(ReceivedRequestsActivity.this, MyProfileActivity.class));
                                } else {
                                    Intent intent = new Intent(ReceivedRequestsActivity.this, UserProfileActivity.class);
                                    intent.putExtra("userId", selectedUid); // ВАЖНО: правилен ключ
                                    startActivity(intent);
                                }
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        DatabaseReference fromRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("friendRequests")
                .child("from");

        fromRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();

                if (!snapshot.exists()) {
                    emptyText.setVisibility(View.VISIBLE);
                    userAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = ds.getKey();
                    if (uid == null) continue;

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    if (userSnap.exists()) {
                                        UserModel user = userSnap.getValue(UserModel.class);
                                        if (user != null) {
                                            requestList.add(user);
                                            userAdapter.notifyItemInserted(requestList.size() - 1);
                                            emptyText.setVisibility(View.GONE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }

                if (snapshot.getChildrenCount() == 0) {
                    emptyText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emptyText.setText("Грешка при зареждане.");
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }
}
