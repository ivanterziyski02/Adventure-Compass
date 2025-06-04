package com.example.adventurecompass.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.adventurecompass.R;
import com.example.adventurecompass.adapters.ReviewAdapter;
import com.example.adventurecompass.models.ReviewModel;
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
                Context context = v.getContext();
                Intent intent = new Intent(context, AddReviewActivity.class);
                intent.putExtra("LOCATION_ID", locationId);
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