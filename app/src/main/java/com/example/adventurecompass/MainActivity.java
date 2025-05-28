package com.example.adventurecompass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button signIn;
    TextView signUp;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        editTextEmail = findViewById(R.id.editText_register_email);
        editTextPassword = findViewById(R.id.editText_register_password);
        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.sign_up);

        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        signIn.setOnClickListener(v -> {
            String email = String.valueOf(editTextEmail.getText());
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(MainActivity.this, "Enter email", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Enter password", Toast.LENGTH_LONG).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser != null) {
                                String uid = currentUser.getUid();
                                String userEmail = currentUser.getEmail();

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
                                                    userMap.put("name", "");
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

                                                    userRef.setValue(userMap);
                                                }else {
                                                    Log.e("FCM_TOKEN", "Неуспешно извличане на токен", task.getException());
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("Firebase", "Database error: " + error.getMessage());
                                    }
                                });

                                Log.d("MainActivity", "ID of logged user: " + uid);
                            }

                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_LONG).show();
                            clearAll();
                        }
                    });
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("🔥FCM", "POST_NOTIFICATIONS permission granted!");
            } else {
                Log.w("🔥FCM", "POST_NOTIFICATIONS permission denied.");
            }
        }
    }

    private void clearAll() {
        editTextEmail.setText("");
        editTextPassword.setText("");
    }
}
