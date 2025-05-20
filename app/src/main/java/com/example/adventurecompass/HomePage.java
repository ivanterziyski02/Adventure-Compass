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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
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

        // Toolbar и RecyclerView
        recyclerView = findViewById(R.id.recyclerview);
        toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Load Locations
        databaseReference = FirebaseDatabase.getInstance().getReference("locations");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locationModelList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LocationModel locationModel = snapshot.getValue(LocationModel.class);
                    if (locationModel != null) {
                        locationModel.setId(snapshot.getKey());
                        locationModelList.add(locationModel);
                    }
                }

                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Грешка при зареждане на локации
            }
        });

        locationAdapter = new LocationAdapter(locationModelList, this);
        recyclerView.setAdapter(locationAdapter);

        // Get profile image
        ShapeableImageView profileImageMini = findViewById(R.id.profileImageMini);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(profileImageMini);
                } else {
                    profileImageMini.setImageResource(R.drawable.ic_person);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profileImageMini.setImageResource(R.drawable.ic_person);
            }
        });

        profileImageMini.setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, MyProfileActivity.class));
        });

    }

    @Override
    public void selectedLocation(LocationModel locationModel) {
        startActivity(new Intent(HomePage.this, SelectedLocationActivity.class).putExtra("data", locationModel));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

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
        return item.getItemId() == R.id.search || super.onOptionsItemSelected(item);
    }
}
