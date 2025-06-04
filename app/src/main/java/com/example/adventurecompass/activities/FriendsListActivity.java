package com.example.adventurecompass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adventurecompass.R;
import com.example.adventurecompass.adapters.UserAdapter;
import com.example.adventurecompass.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private EditText searchField;
    private final List<UserModel> friendList = new ArrayList<>();
    private final List<UserModel> fullFriendList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.requestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchField = findViewById(R.id.searchField);

        adapter = new UserAdapter(this, friendList);
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter.setOnUserClickListener(user -> {
            Intent intent = new Intent(FriendsListActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", user.getUid());
            startActivity(intent);
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadFriends();
    }

    private void filterFriends(String query) {
        List<UserModel> filteredList = new ArrayList<>();
        for (UserModel user : fullFriendList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.updateList(filteredList);
    }

    private void loadFriends() {
        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUserId).child("friends");

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendList.clear();
                fullFriendList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                } else {
                    findViewById(R.id.emptyText).setVisibility(View.GONE);
                }

                List<String> friendUids = new ArrayList<>();
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String uid = friendSnapshot.getKey();
                    if (uid != null) friendUids.add(uid);
                }

                for (String uid : friendUids) {
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    if (userSnap.exists()) {
                                        UserModel user = userSnap.getValue(UserModel.class);
                                        if (user != null) {
                                            user.setUid(userSnap.getKey());

                                            friendList.add(user);
                                            fullFriendList.add(user);
                                        }
                                    } else {
                                        friendsRef.child(uid).removeValue();
                                    }

                                    if (friendList.size() == friendUids.size()) {
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
