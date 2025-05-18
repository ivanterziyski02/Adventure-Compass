package com.example.adventurecompass;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView profileImageView;

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        TextView emailText = findViewById(R.id.emailText);
        TextView nameText = findViewById(R.id.nameText);
        TextView bioText = findViewById(R.id.bioText);
        TextView registrationDateText = findViewById(R.id.registrationDateText);
        profileImageView = findViewById(R.id.profileImageView);
        Button editProfileButton = findViewById(R.id.editProfileButton);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.child("email").getValue(String.class);
                String name = snapshot.child("name").getValue(String.class);
                String bio = snapshot.child("bio").getValue(String.class);
                String imageUrl = snapshot.child("profilePictureUrl").getValue(String.class);
                Long timestamp = snapshot.child("registrationDate").getValue(Long.class);

                emailText.setText("Email: " + (email != null ? email : ""));
                nameText.setText("Name: " + (name != null ? name : ""));
                bioText.setText("Bio: " + (bio != null ? bio : ""));

                if (timestamp != null) {
                    String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(new Date(timestamp));
                    registrationDateText.setText("Регистрация: " + date);
                }

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Need Picasso or Glide library
                    Picasso.get().load(imageUrl).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyProfileActivity.this, "Грешка при зареждане на профила", Toast.LENGTH_SHORT).show();
            }
        });

        editProfileButton.setOnClickListener(v -> {
            showEditDialog(
                    nameText.getText().toString().replace("Name: ", ""),
                    bioText.getText().toString().replace("Bio: ", "")
            );
        });

        profileImageView.setOnClickListener(v -> {
            @SuppressLint("IntentReset") Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        Button buttonChats = findViewById(R.id.buttonChats);
        buttonChats.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, com.example.adventurecompass.chats.ChatListActivity.class);
            startActivity(intent);
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/" + uid + ".png");
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .child("profilePictureUrl")
                                .setValue(imageUrl)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MyProfileActivity.this, "Снимката е качена успешно!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MyProfileActivity.this, "Грешка при запис на URL", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(MyProfileActivity.this, "Неуспешно качване: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void showEditDialog(String currentName, String currentBio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактирай профил");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Име");
        nameInput.setText(currentName);
        layout.addView(nameInput);

        EditText bioInput = new EditText(this);
        bioInput.setHint("Биография");
        bioInput.setText(currentBio);
        layout.addView(bioInput);
        builder.setView(layout);

        builder.setPositiveButton("Запази", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newBio = bioInput.getText().toString().trim();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);
            updates.put("bio", newBio);

            userRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Профилът е обновен", Toast.LENGTH_SHORT).show();
                    recreate();
                } else {
                    Toast.makeText(this, "Грешка при запис", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Отказ", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}