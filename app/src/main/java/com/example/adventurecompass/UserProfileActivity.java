package com.example.adventurecompass;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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
        Button buttonSendRequest = findViewById(R.id.buttonSendRequest);
        Button buttonRequestSent = findViewById(R.id.buttonRequestSent);
        LinearLayout buttonRequestActions = findViewById(R.id.buttonRequestActions);
        LinearLayout friendActions = findViewById(R.id.friendActions);
        Button buttonAccept = findViewById(R.id.buttonAccept);
        Button buttonDecline = findViewById(R.id.buttonDecline);
        Button buttonBlock = findViewById(R.id.buttonBlock);



        // Скриваме всички бутони по подразбиране
        buttonSendRequest.setVisibility(View.GONE);
        buttonRequestSent.setVisibility(View.GONE);
        buttonRequestActions.setVisibility(View.GONE);
        friendActions.setVisibility(View.GONE);



        // Скриваме бутона за редакция
        findViewById(R.id.editProfileButton).setVisibility(android.view.View.GONE);

        // Получаваме uid на избрания потребител
        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Невалиден потребител", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        currentUserRef.child("blocked").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(UserProfileActivity.this, "Този потребител е блокиран", Toast.LENGTH_SHORT).show();
                            finish(); // Затваря екрана
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

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
                // Проверка за връзка между потребителите
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

                currentUserRef.child("friends").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Приятели са
                            friendActions.setVisibility(View.VISIBLE);
                            buttonBlock.setOnClickListener(view -> {
                                DatabaseReference currentUserRef = FirebaseDatabase.getInstance()
                                        .getReference("users").child(currentUserId);

                                // 1. Записваме UID на блокирания потребител
                                currentUserRef.child("blocked").child(userId)
                                        .setValue(true)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // 2. Махаме потребителя от приятели
                                                currentUserRef.child("friends").child(userId).removeValue();
                                                FirebaseDatabase.getInstance().getReference("users")
                                                        .child(userId).child("friends").child(currentUserId).removeValue();

                                                // 3. UI обновяване
                                                friendActions.setVisibility(View.GONE);
                                                Toast.makeText(UserProfileActivity.this, "Потребителят е блокиран", Toast.LENGTH_SHORT).show();
                                                finish(); // или навигиране назад
                                            } else {
                                                Toast.makeText(UserProfileActivity.this, "Грешка при блокиране", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                        } else {
                            // Проверка дали текущият е изпратил покана
                            currentUserRef.child("friendRequests").child("to").child(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshotTo) {
                                            if (snapshotTo.exists()) {
                                                buttonRequestSent.setVisibility(View.VISIBLE);


                                            } else {
                                                // Проверка дали текущият е получил покана
                                                currentUserRef.child("friendRequests").child("from").child(userId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshotFrom) {
                                                                if (snapshotFrom.exists()) {
                                                                    buttonRequestActions.setVisibility(View.VISIBLE);
                                                                    buttonAccept.setOnClickListener(view -> {
                                                                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                                                                        usersRef.child(currentUserId).child("friendRequests").child("from").child(userId).removeValue();
                                                                        usersRef.child(userId).child("friendRequests").child("to").child(currentUserId).removeValue();

                                                                        usersRef.child(currentUserId).child("friends").child(userId).setValue(true);
                                                                        usersRef.child(userId).child("friends").child(currentUserId).setValue(true);

                                                                        Toast.makeText(UserProfileActivity.this, "Добавихте се като приятели", Toast.LENGTH_SHORT).show();
                                                                        buttonRequestActions.setVisibility(View.GONE);
                                                                        friendActions.setVisibility(View.VISIBLE);
                                                                    });

                                                                    buttonDecline.setOnClickListener(view -> {
                                                                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                                                                        usersRef.child(currentUserId).child("friendRequests").child("from").child(userId).removeValue();
                                                                        usersRef.child(userId).child("friendRequests").child("to").child(currentUserId).removeValue();

                                                                        Toast.makeText(UserProfileActivity.this, "Поканата е отказана", Toast.LENGTH_SHORT).show();
                                                                        buttonRequestActions.setVisibility(View.GONE);
                                                                        buttonSendRequest.setVisibility(View.VISIBLE);
                                                                    });

                                                                } else {
                                                                    buttonSendRequest.setVisibility(View.VISIBLE);
                                                                    buttonSendRequest.setOnClickListener(view -> {
                                                                        buttonSendRequest.setEnabled(false);
                                                                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                                                                        usersRef.child(currentUserId).child("friendRequests").child("to").child(userId)
                                                                                .setValue(true)
                                                                                .addOnCompleteListener(task1 -> {
                                                                                    if (task1.isSuccessful()) {
                                                                                        usersRef.child(userId).child("friendRequests").child("from").child(currentUserId)
                                                                                                .setValue(true)
                                                                                                .addOnCompleteListener(task2 -> {
                                                                                                    if (task2.isSuccessful()) {
                                                                                                        Toast.makeText(UserProfileActivity.this, "Поканата е изпратена", Toast.LENGTH_SHORT).show();
                                                                                                        buttonSendRequest.setVisibility(View.GONE);
                                                                                                        buttonRequestSent.setVisibility(View.VISIBLE);
                                                                                                    } else {
                                                                                                        Toast.makeText(UserProfileActivity.this, "Грешка при записване в получателя", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                    } else {
                                                                                        Toast.makeText(UserProfileActivity.this, "Грешка при записване в изпращача", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    });
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {}
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Грешка при зареждане", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
