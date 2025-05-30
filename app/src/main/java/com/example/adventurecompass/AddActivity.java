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
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private EditText userName, description;
    private ImageView imagePreview;
    private Button btnChooseImage, btnAdd, btnBack;
    private Uri selectedImageUri;
    private String userId;
    private String locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        userName = findViewById(R.id.txtName);
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
                clearAll();
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
                            clearAll();
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

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("userName", userName.getText().toString());
        map.put("description", description.getText().toString());
        map.put("url", imageUrl);

        FirebaseDatabase.getInstance().getReference("reviews").child(locationId).push()
                .setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,"!"+ userId+"!", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference("locations").child(locationId).child("name")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                String locationName = snapshot.getValue(String.class);
                                if (locationName != null && !locationName.isEmpty()) {
                                    callSendNotificationFunction(userId, locationName, locationId);
                                } else {
                                    callSendNotificationFunction(userId, "Място",locationId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                callSendNotificationFunction(userId, "Място",locationId);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Грешка при запис", Toast.LENGTH_SHORT).show());
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
        userName.setText("");
        description.setText("");
        imagePreview.setImageResource(R.drawable.ic_launcher_background);
        selectedImageUri = null;
    }
}
