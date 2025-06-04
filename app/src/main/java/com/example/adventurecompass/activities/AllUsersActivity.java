package com.example.adventurecompass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchField;
    private UserAdapter userAdapter;
    private List<UserModel> userList;
    private List<UserModel> fullUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        recyclerView = findViewById(R.id.recyclerUsers);
        searchField = findViewById(R.id.searchField);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        fullUserList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(user -> {
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.orderByChild("email").equalTo(user.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String selectedUid = child.getKey();
                                if (selectedUid.equals(currentUid)) {
                                    startActivity(new Intent(AllUsersActivity.this, MyProfileActivity.class));
                                } else {
                                    Intent intent = new Intent(AllUsersActivity.this, UserProfileActivity.class);
                                    intent.putExtra("userId", selectedUid);
                                    startActivity(intent);
                                }
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadUsers();
    }

    private void loadUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserModel> tempList = new ArrayList<>();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    UserModel user = userSnap.getValue(UserModel.class);
                    if (user != null) {
                        tempList.add(user);
                    }
                }
                fullUserList.clear();
                fullUserList.addAll(tempList);
                userAdapter.updateList(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterUsers(String query) {
        List<UserModel> filteredList = new ArrayList<>();
        for (UserModel user : fullUserList) {
            String name = user.getName() != null ? user.getName().toLowerCase() : "";
            String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

            if (name.contains(query.toLowerCase()) || email.contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList);
    }
}
