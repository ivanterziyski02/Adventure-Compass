package com.example.adventurecompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;

public class ReviewActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ReviewAdapter reviewAdapter;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String locationId = getIntent().getStringExtra("LOCATION_ID");
        Log.d("ReviewActivity", "Received in review location ID: " + locationId);

        assert locationId != null;
        FirebaseRecyclerOptions<ReviewModel> options =
                new FirebaseRecyclerOptions.Builder<ReviewModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("reviews").child(locationId), ReviewModel.class)
                        .build();

        reviewAdapter = new ReviewAdapter(options,locationId);
        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setItemAnimator(null);


        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the context from the view
                Context context = v.getContext();
                Intent intent = new Intent(context, AddActivity.class);
                intent.putExtra("LOCATION_ID", locationId); //locationId -> AddActivity
                context.startActivity(intent);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (reviewAdapter != null) {
            reviewAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reviewAdapter != null) {
            reviewAdapter.stopListening();
        }
    }
}