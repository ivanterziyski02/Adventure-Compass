package com.example.adventurecompass;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends FirebaseRecyclerAdapter<ReviewModel, ReviewAdapter.myViewHolder> {
    private final String locationId;

    public ReviewAdapter(@NonNull FirebaseRecyclerOptions<ReviewModel> options, String locationId) {
        super(options);
        this.locationId = locationId;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull ReviewModel model) {
        holder.userName.setText(model.getUserName());
        holder.description.setText(model.getDescription());

        Glide.with(holder.img.getContext())
                .load(model.getUrl())
                .placeholder(com.google.firebase.database.R.drawable.common_google_signin_btn_icon_dark)
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.img);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAuthor = currentUser != null && currentUser.getUid().equals(model.getUserId());

        holder.btnEdit.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> {
            if (!isAuthor) {
                Toast.makeText(holder.itemView.getContext(), "You are not the author of this review", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(holder.itemView.getContext(), EditActivity.class);
            intent.putExtra("reviewId", getRef(position).getKey());
            intent.putExtra("locationId", locationId);
            intent.putExtra("userName", model.getUserName());
            intent.putExtra("description", model.getDescription());
            intent.putExtra("imageUrl", model.getUrl());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (!isAuthor) {
                Toast.makeText(holder.itemView.getContext(), "You are not the author of this review", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Are you sure?");
            builder.setMessage("Deleted data cannot be undone.");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                FirebaseDatabase.getInstance().getReference("reviews")
                        .child(locationId)
                        .child(Objects.requireNonNull(getRef(position).getKey()))
                        .removeValue();
            });

            builder.setNegativeButton("Cancel", (dialog, which) ->
                    Toast.makeText(holder.itemView.getContext(), "Cancelled", Toast.LENGTH_SHORT).show());

            builder.show();
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        CircleImageView img;
        TextView userName, description;
        Button btnEdit, btnDelete;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgLoc);
            userName = itemView.findViewById(R.id.nametext);
            description = itemView.findViewById(R.id.descriptiontext);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
