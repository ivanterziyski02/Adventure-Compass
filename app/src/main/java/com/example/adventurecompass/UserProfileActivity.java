package com.example.adventurecompass;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.adventurecompass.friendship.FriendshipManager;
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
    private FriendshipManager friendshipManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        friendshipManager = new FriendshipManager(this);

        emailText = findViewById(R.id.emailText);
        nameText = findViewById(R.id.nameText);
        bioText = findViewById(R.id.bioText);
        registrationDateText = findViewById(R.id.registrationDateText);
        profileImageView = findViewById(R.id.profileImageView);

        Button buttonSendRequest = findViewById(R.id.buttonSendRequest);
        Button buttonRequestSent = findViewById(R.id.buttonRequestSent);
        LinearLayout buttonRequestActions = findViewById(R.id.buttonRequestActions);
        LinearLayout friendActions = findViewById(R.id.friendActions);
        LinearLayout blockActionsLayout = findViewById(R.id.blockActionsLayout);
        Button buttonAccept = findViewById(R.id.buttonAccept);
        Button buttonDecline = findViewById(R.id.buttonDecline);
        Button buttonBlock = findViewById(R.id.buttonBlock);
        Button buttonUnblock = findViewById(R.id.buttonUnblock);


        buttonSendRequest.setVisibility(View.GONE);
        buttonRequestSent.setVisibility(View.GONE);
        buttonRequestActions.setVisibility(View.GONE);
        friendActions.setVisibility(View.GONE);
        blockActionsLayout.setVisibility(View.GONE);
        buttonBlock.setVisibility(View.GONE);
        findViewById(R.id.editProfileButton).setVisibility(View.GONE);

        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Невалиден потребител", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId.equals(currentUserId)) {
            startActivity(new Intent(this, MyProfileActivity.class));
            finish();
            return;
        }

        // Get user info
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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

                // Get state between users
                friendshipManager.getRelationshipState(currentUserId, userId, state -> {
                    switch (state) {
                        case BLOCKED:
                            Toast.makeText(UserProfileActivity.this, "Този потребител е блокиран", Toast.LENGTH_SHORT).show();
                            blockActionsLayout.setVisibility(View.VISIBLE);
                            buttonUnblock.setOnClickListener(v ->
                                    friendshipManager.unblockUser(currentUserId, userId, UserProfileActivity.this::recreate)
                            );
                            break;

                        case FRIENDS:
                            friendActions.setVisibility(View.VISIBLE);
                            buttonBlock.setVisibility(View.VISIBLE);
                            buttonBlock.setOnClickListener(v ->
                                    friendshipManager.blockUser(currentUserId, userId, UserProfileActivity.this::finish)
                            );
                            break;

                        case REQUEST_SENT:
                            buttonRequestSent.setVisibility(View.VISIBLE);
                            break;

                        case REQUEST_RECEIVED:
                            buttonRequestActions.setVisibility(View.VISIBLE);
                            buttonAccept.setOnClickListener(v ->
                                    friendshipManager.acceptFriendRequest(currentUserId, userId, () -> {
                                        buttonRequestActions.setVisibility(View.GONE);
                                        friendActions.setVisibility(View.VISIBLE);
                                        buttonBlock.setVisibility(View.VISIBLE);
                                    })
                            );
                            buttonDecline.setOnClickListener(v ->
                                    friendshipManager.declineFriendRequest(currentUserId, userId, () -> {
                                        buttonRequestActions.setVisibility(View.GONE);
                                        buttonSendRequest.setVisibility(View.VISIBLE);
                                    })
                            );
                            break;

                        case NO_RELATION:
                            buttonSendRequest.setVisibility(View.VISIBLE);
                            buttonSendRequest.setOnClickListener(v ->
                                    friendshipManager.sendFriendRequest(currentUserId, userId, () -> {
                                        buttonSendRequest.setVisibility(View.GONE);
                                        buttonRequestSent.setVisibility(View.VISIBLE);
                                    })
                            );
                            break;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Грешка при зареждане", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
