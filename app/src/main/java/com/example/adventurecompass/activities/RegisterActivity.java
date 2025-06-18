package com.example.adventurecompass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adventurecompass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextUserName, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button register;
    TextView signIn;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUserName = findViewById(R.id.editText_register_full_name);
        editTextPassword = findViewById(R.id.editText_register_password);
        editTextConfirmPassword = findViewById(R.id.editText_register_confirm_password);
        editTextEmail = findViewById(R.id.editText_register_email);
        signIn = findViewById(R.id.sign_in);
        register = findViewById(R.id.sign_up);

        signIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        register.setOnClickListener(v -> {
            String email = String.valueOf(editTextEmail.getText()).trim();
            String password = String.valueOf(editTextPassword.getText()).trim();
            String confirmPassword = String.valueOf(editTextConfirmPassword.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(RegisterActivity.this, "Въведи имейл.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!email.contains("@")) {
                Toast.makeText(RegisterActivity.this, "Имейлът трябва да съдържа '@'.", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterActivity.this, "Въведи парола.", Toast.LENGTH_LONG).show();
                return;
            }
            if (password.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Паролата трябва да е поне 8 символа.", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Потвърди парола.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Паролите не съвпадат.", Toast.LENGTH_LONG).show();
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser != null) {
                                String uid = currentUser.getUid();
                                String userEmail = currentUser.getEmail();
                                String username = String.valueOf(editTextUserName.getText()).trim();

                                DatabaseReference userRef = FirebaseDatabase.getInstance()
                                        .getReference("users").child(uid);

                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                                if (tokenTask.isSuccessful()) {
                                                    String token = tokenTask.getResult();
                                                    Map<String, Object> userMap = new HashMap<>();
                                                    userMap.put("name", username);
                                                    userMap.put("bio", "");
                                                    userMap.put("profilePictureUrl", "");
                                                    userMap.put("email", userEmail);
                                                    userMap.put("registrationDate", ServerValue.TIMESTAMP);
                                                    userMap.put("fcmToken", token);
                                                    userMap.put("friends", new HashMap<>());

                                                    Map<String, Object> friendRequests = new HashMap<>();
                                                    friendRequests.put("from", new HashMap<>());
                                                    friendRequests.put("to", new HashMap<>());
                                                    userMap.put("friendRequests", friendRequests);

                                                    userRef.setValue(userMap).addOnCompleteListener(writeTask -> {
                                                        if (writeTask.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Регистрацията е успешна", Toast.LENGTH_LONG).show();
                                                            Intent intent = new Intent(RegisterActivity.this, HomePageActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Грешка при записване на данни", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            Intent intent = new Intent(RegisterActivity.this, HomePageActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(RegisterActivity.this, "Грешка при връзка с базата", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Опитайте отново", Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
