package com.example.adventurecompass.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.adventurecompass.R;
import com.example.adventurecompass.models.ReviewModel;
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
        locationId = getIntent().getStringExtra("LOCATION_ID");

        if (review == null || reviewId  == null || locationId == null ) {
            Toast.makeText(this, "Грешка при отварянето на ревюто", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Сигурни ли сте?");
                builder.setMessage("Изтритите данни не могат да бъдат възстановени.");

                builder.setPositiveButton("Изтрий", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference("reviews")
                            .child(locationId)
                            .child(reviewId)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Изтрито", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });

                builder.setNegativeButton("Отказ", (dialog, which) ->
                        Toast.makeText(this, "Отказано", Toast.LENGTH_SHORT).show());

                AlertDialog dialog = builder.create();

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffe0b2")));
                dialog.show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                if (positiveButton != null) {
                    positiveButton.setTextColor(Color.WHITE);
                    positiveButton.setBackgroundColor(Color.parseColor("#e53935"));
                }

                if (negativeButton != null) {
                    negativeButton.setTextColor(Color.WHITE);
                    negativeButton.setBackgroundColor(Color.parseColor("#757575"));
                }
            });

        }
    }
}
