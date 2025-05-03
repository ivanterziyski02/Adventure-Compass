package com.example.adventurecompass;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends FirebaseRecyclerAdapter<ReviewModel,ReviewAdapter.myViewHolder> {
    private String locationId;
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
        if (currentUser != null && currentUser.getUid().equals(model.getUserId())) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting id of the user
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getUid().equals(model.getUserId())) {

                    final DialogPlus dialogPlus = DialogPlus.newDialog(holder.img.getContext())
                            .setContentHolder(new ViewHolder(R.layout.update_popup))
                            .setExpanded(true, 1200)
                            .create();

                    View view = dialogPlus.getHolderView();

                    EditText userName = view.findViewById(R.id.txtName);
                    EditText description = view.findViewById(R.id.txtDescription);
                    EditText url = view.findViewById(R.id.txtImageUrl);

                    Button btnUpdate = view.findViewById(R.id.btnUpdate);

                    userName.setText(model.getUserName());
                    description.setText(model.getDescription());
                    url.setText(model.getUrl());

                    dialogPlus.show();

                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("userName", userName.getText().toString());
                            map.put("description", description.getText().toString());
                            map.put("url", url.getText().toString());

                            FirebaseDatabase.getInstance().getReference("reviews").child(locationId)
                                    .child(getRef(position).getKey()).updateChildren(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(holder.userName.getContext(), "Successfully updated", Toast.LENGTH_SHORT).show();
                                            dialogPlus.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(holder.userName.getContext(), "Updating process failed", Toast.LENGTH_SHORT).show();
                                            dialogPlus.dismiss();
                                        }
                                    });
                        }
                    });

                }else{
                    Toast.makeText(holder.img.getContext(), "You are not the author of this review", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null && currentUser.getUid().equals(model.getUserId())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.userName.getContext());
                    builder.setTitle("Are you Sure?");
                    builder.setMessage("Deleted data can not be undo.");

                    builder.setPositiveButton("Deleted", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference("reviews").child(locationId)
                                    .child(getRef(position).getKey()).removeValue();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(holder.userName.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }else{
                    Toast.makeText(holder.img.getContext(), "You are not the author of this review", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item,parent,false);

        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        CircleImageView img;
        TextView userName, description;
        Button btnEdit,btnDelete;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imgLoc);
            userName = itemView.findViewById(R.id.nametext);
            description = itemView.findViewById(R.id.descriptiontext);

            btnEdit =  itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
