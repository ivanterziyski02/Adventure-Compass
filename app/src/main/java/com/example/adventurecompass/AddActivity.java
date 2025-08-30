package com.example.adventurecompass;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private EditText description;
    private ImageView imagePreview;
    private Button btnChooseImage, btnAdd, btnBack;
    private Uri selectedImageUri;
    private String userId, locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        description = findViewById(R.id.txtEmail);
        imagePreview = findViewById(R.id.imagePreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);

        locationId = getIntent().getStringExtra("LOCATION_ID");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        btnChooseImage.setOnClickListener(v -> {
            @SuppressLint("IntentReset")
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnAdd.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToFirebase();
            } else {
                insertData(locationId, "");
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imagePreview.setImageURI(selectedImageUri);
                        Toast.makeText(this, "Снимката е избрана успешно", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void uploadImageToFirebase() {
        String filename = "images/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(filename);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            insertData(locationId, imageUrl);
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Неуспешно качване: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void insertData(String locationId, String imageUrl) {
        if (userId == null || locationId == null || locationId.isEmpty()) {
            Toast.makeText(this, "Невалиден потребител или място", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);
                if (name == null || name.isEmpty()) name = "Anonymous";

                Map<String, Object> map = new HashMap<>();
                map.put("userId", userId);
                map.put("userName", name);
                map.put("description", description.getText().toString());
                map.put("locationImageUrl", imageUrl);
                map.put("profilePictureUrl", profileUrl != null ? profileUrl : "");

                FirebaseDatabase.getInstance().getReference("reviews").child(locationId).push()
                        .setValue(map)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(AddActivity.this, "Ревюто е записано", Toast.LENGTH_SHORT).show();

                            FirebaseDatabase.getInstance().getReference("locations").child(locationId).child("name")
                                    .get()
                                    .addOnSuccessListener(locationSnapshot -> {
                                        String locationName = locationSnapshot.getValue(String.class);
                                        if (locationName != null && !locationName.isEmpty()) {
                                            callSendNotificationFunction(userId, locationName, locationId);
                                        } else {
                                            callSendNotificationFunction(userId, "Място", locationId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        callSendNotificationFunction(userId, "Място", locationId);
                                    });

                            clearAll();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(AddActivity.this, "Грешка при запис", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddActivity.this, "Грешка при четене на потребител", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callSendNotificationFunction(String userId, String locationName, String locationId) {
        if (userId == null || locationName == null || locationId == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("locationName", locationName);
        data.put("LOCATION_ID", locationId);

        FirebaseFunctions.getInstance("us-central1")
                .getHttpsCallable("sendNotificationOnReview")
                .call(data)
                .addOnSuccessListener(result -> {Object resultData = result.getData();

                    if (resultData instanceof Map) {
                        Map<?, ?> resultMap = (Map<?, ?>) resultData;
                        Object success = resultMap.get("success");
                    }

                    Toast.makeText(this, "Известията са изпратени", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Грешка при изпращането: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearAll() {
        description.setText("");
        imagePreview.setImageResource(R.drawable.ic_launcher_background);
        selectedImageUri = null;
    }
}
