package com.example.adventurecompass;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends FirebaseRecyclerAdapter<UserModel, UserAdapter.UserViewHolder> {

    public UserAdapter(@NonNull FirebaseRecyclerOptions<UserModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {
        holder.userName.setText(model.getName());
        holder.userEmail.setText(model.getEmail());

        Glide.with(holder.userImage.getContext())
                .load(model.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person) // показва се докато зарежда
                .error(R.drawable.ic_person)       // ако няма или невалиден URL
                .into(holder.userImage);

        holder.itemView.setOnClickListener(v -> {
            String userId = getRef(position).getKey(); // Взима ключа (UID) от Firebase
            if (userId != null) {
                Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                intent.putExtra("userId", userId);
                v.getContext().startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail;
        CircleImageView userImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }
}
