package com.example.adventurecompass;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class AllUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        recyclerView = findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("users"), UserModel.class)
                        .build();

        userAdapter = new UserAdapter(options);
        recyclerView.setAdapter(userAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userAdapter != null) userAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userAdapter != null) userAdapter.stopListening();
    }
}