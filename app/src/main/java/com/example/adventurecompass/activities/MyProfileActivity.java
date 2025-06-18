package com.example.adventurecompass.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.example.adventurecompass.R;
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
        Button friendsButton = findViewById(R.id.buttonFriends);
        Button logOutButton = findViewById(R.id.buttonLogout);
        Button allUsersButton = findViewById(R.id.buttonAllUsers);
        Button receivedRequestsButton = findViewById(R.id.buttonFriendRequests);
        Button buttonChats = findViewById(R.id.buttonChats);

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

                emailText.setText("Имейл: " + (email != null ? email : ""));
                nameText.setText("Име: " + (name != null ? name : ""));
                bioText.setText("Био: " + (bio != null ? bio : ""));

                if (timestamp != null) {
                    String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(new Date(timestamp));
                    registrationDateText.setText("Регистрация: " + date);
                }

                if (imageUrl != null && !imageUrl.isEmpty()) {
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

        buttonChats.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        allUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, AllUsersActivity.class);
            startActivity(intent);
        });

        friendsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyProfileActivity.this, FriendsListActivity.class);
            startActivity(intent);
        });

        receivedRequestsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MyProfileActivity.this, ReceivedRequestsActivity.class);
            startActivity(intent);
        });

        logOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5d29c")));
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setTextColor(Color.WHITE);
            positiveButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        if (negativeButton != null) {
            negativeButton.setTextColor(Color.WHITE);
            negativeButton.setBackgroundColor(Color.parseColor("#F44336"));
        }
    }
}