package com.example.adventurecompass;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    private EditText userNameEditText, descriptionEditText;
    private ImageView imagePreview;
    private Button btnChooseImage, btnUpdate;

    private String reviewId, locationId, currentImageUrl;
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> galleryLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_popup); // Използваме вече създадения layout

        // Връзки към елементи
        userNameEditText = findViewById(R.id.txtName);
        descriptionEditText = findViewById(R.id.txtDescription);
        imagePreview = findViewById(R.id.imagePreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Получаване на данни от Intent
        Intent intent = getIntent();
        reviewId = intent.getStringExtra("reviewId");
        locationId = intent.getStringExtra("locationId");
        String userName = intent.getStringExtra("userName");
        String description = intent.getStringExtra("description");
        currentImageUrl = intent.getStringExtra("imageUrl");

        // Попълване на текущи стойности
        userNameEditText.setText(userName);
        descriptionEditText.setText(description);
        Glide.with(this).load(currentImageUrl).into(imagePreview);

        // Избор на нова снимка
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imagePreview.setImageURI(selectedImageUri);
                    }
                }
        );

        btnChooseImage.setOnClickListener(v -> {
            Intent intentGallery = new Intent(Intent.ACTION_PICK);
            intentGallery.setType("image/*");
            galleryLauncher.launch(intentGallery);
        });

        btnUpdate.setOnClickListener(v -> {
            if (reviewId == null || locationId == null) {
                Toast.makeText(this, "Липсва информация за ревю", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageAndUpdate();
            } else {
                updateReview(currentImageUrl);
            }
        });
    }

    private void uploadImageAndUpdate() {
        String filename = "images/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(filename);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> updateReview(uri.toString()))
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Неуспешно качване: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateReview(String imageUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("userName", userNameEditText.getText().toString().trim());
        map.put("description", descriptionEditText.getText().toString().trim());
        map.put("url", imageUrl);

        FirebaseDatabase.getInstance().getReference("reviews")
                .child(locationId).child(reviewId).updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Ревюто е обновено успешно", Toast.LENGTH_SHORT).show();
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(this::finish, 800);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Грешка при обновяване", Toast.LENGTH_SHORT).show());
    }
}
