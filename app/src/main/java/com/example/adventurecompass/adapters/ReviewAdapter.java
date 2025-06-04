package com.example.adventurecompass.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.adventurecompass.R;
import com.example.adventurecompass.models.ReviewModel;
import com.example.adventurecompass.activities.EditActivity;
import com.example.adventurecompass.activities.ReviewDetailActivity;
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
        String fullText = model.getDescription();
        String[] words = fullText.trim().split("\\s+");

        if (words.length > 7) {
            StringBuilder shortText = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                shortText.append(words[i]).append(" ");
            }
            shortText.append("... View more");
            holder.description.setText(shortText.toString().trim());
        } else {
            holder.description.setText(fullText);
        }

        // Load location image
        Glide.with(holder.locationImage.getContext())
                .load(model.getLocationImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.locationImage);

        // Show/hide edit/delete buttons if current user is the author
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAuthor = currentUser != null && currentUser.getUid().equals(model.getUserId());

        holder.btnEdit.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

        // Click opens detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ReviewDetailActivity.class);
            intent.putExtra("review", model);
            intent.putExtra("reviewId", getRef(position).getKey());
            intent.putExtra("LOCATION_ID", locationId);
            holder.itemView.getContext().startActivity(intent);
        });

        // Edit button logic
        holder.btnEdit.setOnClickListener(v -> {
            if (!isAuthor) {
                Toast.makeText(holder.itemView.getContext(), "Не сте автор на това мнение", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(holder.itemView.getContext(), EditActivity.class);
            intent.putExtra("reviewId", getRef(position).getKey());
            intent.putExtra("locationId", locationId);
            intent.putExtra("userName", model.getUserName());
            intent.putExtra("description", model.getDescription());
            intent.putExtra("imageUrl", model.getLocationImageUrl());
            holder.itemView.getContext().startActivity(intent);
        });

        // Delete button logic
        holder.btnDelete.setOnClickListener(v -> {
            if (!isAuthor) {
                Toast.makeText(holder.itemView.getContext(), "Не сте автор на това мнение", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Сигурни ли сте?");
            builder.setMessage("Изтритите данни не могат да бъдат възстановени.");

            builder.setPositiveButton("Изтрий", (dialog, which) -> {
                FirebaseDatabase.getInstance().getReference("reviews")
                        .child(locationId)
                        .child(Objects.requireNonNull(getRef(position).getKey()))
                        .removeValue();
            });

            builder.setNegativeButton("Отказ", (dialog, which) ->
                    Toast.makeText(holder.itemView.getContext(), "Отказано", Toast.LENGTH_SHORT).show());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffe0b2")));
            dialog.show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.WHITE);
                positiveButton.setBackgroundColor(Color.parseColor("#e53935"));
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.WHITE);
                negativeButton.setBackgroundColor(Color.parseColor("#757575"));
            }
        });

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new myViewHolder(view);
    }

    static class myViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        CircleImageView locationImage;
        TextView userName, description;
        Button btnEdit, btnDelete;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            locationImage = itemView.findViewById(R.id.imgLoc);
            userName = itemView.findViewById(R.id.nametext);
            description = itemView.findViewById(R.id.descriptiontext);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
