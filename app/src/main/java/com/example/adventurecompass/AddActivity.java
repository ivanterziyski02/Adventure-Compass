package com.example.adventurecompass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {
    EditText userName,description,url;
    Button btnAdd,btnBack;

    private String locationId;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // Извличане на locationId от Intent
        locationId = getIntent().getStringExtra("LOCATION_ID");
        Log.d("AddActivity", "Da vidim dali pristiga: " + locationId);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid(); // Това е уникалният UID за потребителя
            Log.d("AddActivity", "ID-to stigna li do add: " + userId);
        }else{
            Log.d("AddActivity", "Null e");
        }

        userName = findViewById(R.id.txtName);
        description = findViewById(R.id.txtEmail);
        url = findViewById(R.id.txtImageUrl);

        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData(locationId);
                clearAll();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void insertData(String locationId)
    {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("userName",userName.getText().toString());
        map.put("description",description.getText().toString());
        map.put("url",url.getText().toString());
        if (locationId != null && !locationId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("reviews").child(locationId).push()
                    .setValue(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(AddActivity.this, "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            Toast.makeText(AddActivity.this, "Invalid Location ID "+locationId, Toast.LENGTH_SHORT).show();
        }
    }
    private void clearAll(){
        userName.setText("");
        description.setText("");
        url.setText("");
    }
}
