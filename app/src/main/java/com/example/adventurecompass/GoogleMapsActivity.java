package com.example.adventurecompass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.location.Geocoder;
import android.location.Address;
import java.util.List;
import java.util.Locale;




public class GoogleMapsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText editTextSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        editTextSource = findViewById(R.id.source);
        EditText editTextDestination = findViewById(R.id.destination);
        Button button = findViewById(R.id.btnSubmit);

        // Извличане на данни от Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("LOCATION_NAME")) {
            String locationName = intent.getStringExtra("LOCATION_NAME");
            editTextDestination.setText(locationName); // Задаване на дестинацията
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Проверка за разрешение
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = editTextSource.getText().toString();
                String destination = editTextDestination.getText().toString();
                if (source.equals("") | destination.equals("")){
                    Toast.makeText(getApplicationContext(),"Enter both source and destination",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Uri uri = Uri.parse("https://www.google.com/maps/dir/" + source + "/" +destination);
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setPackage("com.google.android.apps.maps");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String addressLine = address.getAddressLine(0); // пълният адрес
                        editTextSource.post(() -> editTextSource.setText(addressLine));
                    } else {
                        editTextSource.setText(latitude + "," + longitude); // fallback
                    }
                } catch (Exception e) {
                    editTextSource.setText(latitude + "," + longitude); // fallback при грешка
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Локацията е недостъпна", Toast.LENGTH_SHORT).show();
            }
        });
        /*
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        editTextSource.setText(latitude + "," + longitude);
                    }
                });

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        editTextSource.setText(latitude + "," + longitude);
                    } else {
                        Toast.makeText(this, "Локацията е null (не е налична)", Toast.LENGTH_LONG).show();
                    }
                });

         */
    }
}