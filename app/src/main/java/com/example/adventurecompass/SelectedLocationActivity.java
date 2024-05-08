package com.example.adventurecompass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SelectedLocationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_location);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            LocationModel locationModel = (LocationModel) intent.getSerializableExtra("data");
            assert locationModel != null;
            startActivity(new Intent(SelectedLocationActivity.this,ReviewActivity.class).putExtra("LOCATION_ID", locationModel.getId()));
            finish();
        }
    }
}