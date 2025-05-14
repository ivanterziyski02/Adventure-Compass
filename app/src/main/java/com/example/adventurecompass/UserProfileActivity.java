package com.example.adventurecompass;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView emailText, nameText, bioText, registrationDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile); // Използваме същия layout

        emailText = findViewById(R.id.emailText);
        nameText = findViewById(R.id.nameText);
        bioText = findViewById(R.id.bioText);
        registrationDateText = findViewById(R.id.registrationDateText);
        profileImageView = findViewById(R.id.profileImageView);

        // Скриваме бутона за редакция
        findViewById(R.id.editProfileButton).setVisibility(android.view.View.GONE);

        // Получаваме uid на избрания потребител
        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Невалиден потребител", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Зареждаме потребителя от Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
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
                    Picasso.get().load(imageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_person);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Грешка при зареждане", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
