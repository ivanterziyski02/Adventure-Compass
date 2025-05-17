package com.example.adventurecompass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ProgressBar progressBar;
    private final List<UserModel> friendList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(this, friendList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter.setOnUserClickListener(user -> {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.orderByChild("email").equalTo(user.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String selectedUid = child.getKey();
                                    Intent intent = new Intent(FriendsListActivity.this, UserProfileActivity.class);
                                    intent.putExtra("userId", selectedUid);
                                    startActivity(intent);
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });

        loadFriends();
    }

    private void loadFriends() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUserId).child("friends");

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String uid = friendSnapshot.getKey();
                    if (uid == null) continue;

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    if (userSnap.exists()) {
                                        UserModel user = userSnap.getValue(UserModel.class);
                                        if (user != null) {
                                            friendList.add(user);
                                            adapter.notifyItemInserted(friendList.size() - 1);
                                        }
                                    } else {
                                        friendsRef.child(uid).removeValue();
                                    }

                                    if (friendList.size() == snapshot.getChildrenCount()) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
