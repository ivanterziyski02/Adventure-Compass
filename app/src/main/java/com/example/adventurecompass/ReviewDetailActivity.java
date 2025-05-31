package com.example.adventurecompass;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewDetailActivity extends AppCompatActivity {

    private ImageView locationImage;
    private CircleImageView profileImage;
    private TextView userName, fullDescription;
    private Button btnEdit, btnDelete;
    private ReviewModel review;

    private String reviewId;
    private String locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        review = getIntent().getParcelableExtra("review");
        reviewId = getIntent().getStringExtra("reviewId");
        locationId = getIntent().getStringExtra("locationId");

        locationImage = findViewById(R.id.locationImage);
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userNameText);
        fullDescription = findViewById(R.id.reviewText);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        Glide.with(this).load(review.getLocationImageUrl()).into(locationImage);
        userName.setText(review.getUserName());
        fullDescription.setText(review.getDescription());

        FirebaseDatabase.getInstance().getReference("users")
                .child(review.getUserId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);
                    Glide.with(this)
                            .load(profileUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(profileImage);
                });

        View.OnClickListener profileClick = v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", review.getUserId());
            startActivity(intent);
        };

        profileImage.setOnClickListener(profileClick);
        userName.setOnClickListener(profileClick);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAuthor = currentUser != null && currentUser.getUid().equals(review.getUserId());

        if (isAuthor) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("reviewId", reviewId);
                intent.putExtra("locationId", locationId);
                intent.putExtra("userName", review.getUserName());
                intent.putExtra("description", review.getDescription());
                intent.putExtra("imageUrl", review.getLocationImageUrl());
                startActivity(intent);
            });

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure?")
                        .setMessage("Deleted data cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseDatabase.getInstance().getReference("reviews")
                                    .child(locationId)
                                    .child(reviewId)
                                    .removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                        finish(); // затваря екрана
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
}
