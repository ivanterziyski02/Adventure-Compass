package com.example.adventurecompass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewDetailActivity extends AppCompatActivity {
    ImageView locationImage;
    CircleImageView profileImage;
    TextView userName, fullDescription;
    ReviewModel review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        review = getIntent().getParcelableExtra("review");

        locationImage = findViewById(R.id.locationImage);
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userNameText);
        fullDescription = findViewById(R.id.reviewText);

        Glide.with(this).load(review.getLocationImageUrl()).into(locationImage);
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

        userName.setText(review.getUserName());
        fullDescription.setText(review.getDescription());

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", review.getUserId());
            startActivity(intent);
        });

        userName.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("userId", review.getUserId());
            startActivity(intent);
        });
    }
}
