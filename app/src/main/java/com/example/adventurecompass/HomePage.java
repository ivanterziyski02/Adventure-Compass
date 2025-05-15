package com.example.adventurecompass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity implements LocationAdapter.SelectedLocation {
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<LocationModel> locationModelList = new ArrayList<>();
    DatabaseReference databaseReference;
    LocationAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        recyclerView = findViewById(R.id.recyclerview);
        toolbar = findViewById(R.id.toolbar);

        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference("locations");

        // Read from the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locationModelList.clear(); // Clear the list before adding to it

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LocationModel locationModel = snapshot.getValue(LocationModel.class);
                    if (locationModel != null) {
                        locationModel.setId(snapshot.getKey());
                    }
                    locationModelList.add(locationModel); // Add location from database to list
                }

                locationAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        locationAdapter = new LocationAdapter(locationModelList, (LocationAdapter.SelectedLocation) this);
        recyclerView.setAdapter(locationAdapter);

        Button myProfileButton = findViewById(R.id.myProfileButton);
        myProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MyProfileActivity.class);
            startActivity(intent);
        });

        Button allUsersButton = findViewById(R.id.allUsersButton);
        allUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, AllUsersActivity.class); // Ще създадем това Activity
            startActivity(intent);
        });

    }
    @Override
    public void selectedLocation(LocationModel locationModel) {
        startActivity(new Intent(HomePage.this,SelectedLocationActivity.class).putExtra("data", locationModel));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);

        MenuItem menuItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                locationAdapter.getFilter().filter(newText);
                return true;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.search){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}