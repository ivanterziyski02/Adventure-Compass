package com.example.adventurecompass;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ReceivedRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TextView emptyText;
    private DatabaseReference usersRef, currentUserRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_requests);

        recyclerView = findViewById(R.id.requestsRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        currentUserRef.child("friendRequests").child("from")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            emptyText.setVisibility(View.VISIBLE);
                            return;
                        }

                        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.Builder<UserModel>()
                                .setIndexedQuery(
                                        currentUserRef.child("friendRequests").child("from"),
                                        usersRef,
                                        UserModel.class)
                                .build();

                        userAdapter = new UserAdapter(options);
                        recyclerView.setAdapter(userAdapter);
                        userAdapter.startListening();

                        emptyText.setVisibility(View.GONE); // има резултати – скриваме текста
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        emptyText.setText("Грешка при зареждане.");
                        emptyText.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userAdapter != null) {
            userAdapter.stopListening();
        }
    }
}
