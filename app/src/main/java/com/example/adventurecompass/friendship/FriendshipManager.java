package com.example.adventurecompass.friendship;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendshipManager {
    private final Context context;
    private final DatabaseReference usersRef;
    public FriendshipManager(Context context) {
        this.context = context;
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }
    public void getRelationshipState(String currentUid, String targetUid, RelationshipCallback callback) {
        usersRef.child(currentUid).child("blocked").child(targetUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot blockedSnapshot) {
                        if (blockedSnapshot.exists()) {
                            callback.onResult(RelationshipState.BLOCKED);
                            return;
                        }

                        usersRef.child(currentUid).child("friends").child(targetUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                                        if (friendSnapshot.exists()) {
                                            callback.onResult(RelationshipState.FRIENDS);
                                            return;
                                        }

                                        usersRef.child(currentUid).child("friendRequests").child("to").child(targetUid)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override public void onDataChange(@NonNull DataSnapshot toSnapshot) {
                                                        if (toSnapshot.exists()) {
                                                            callback.onResult(RelationshipState.REQUEST_SENT);
                                                            return;
                                                        }

                                                        usersRef.child(currentUid).child("friendRequests").child("from").child(targetUid)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override public void onDataChange(@NonNull DataSnapshot fromSnapshot) {
                                                                        if (fromSnapshot.exists()) {
                                                                            callback.onResult(RelationshipState.REQUEST_RECEIVED);
                                                                        } else {
                                                                            callback.onResult(RelationshipState.NO_RELATION);
                                                                        }
                                                                    }

                                                                    @Override public void onCancelled(@NonNull DatabaseError error) {
                                                                        showToast("Грешка при проверка на заявките");
                                                                    }
                                                                });
                                                    }

                                                    @Override public void onCancelled(@NonNull DatabaseError error) {
                                                        showToast("Грешка при проверка на заявките");
                                                    }
                                                });
                                    }

                                    @Override public void onCancelled(@NonNull DatabaseError error) {
                                        showToast("Грешка при проверка на приятелството");
                                    }
                                });
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        showToast("Грешка при проверка за блокиране");
                    }
                });
    }

    public void sendFriendRequest(String currentUid, String targetUid, Runnable onSuccess) {
        usersRef.child(currentUid).child("friendRequests").child("to").child(targetUid).setValue(true)
                .addOnSuccessListener(aVoid ->
                        usersRef.child(targetUid).child("friendRequests").child("from").child(currentUid).setValue(true)
                                .addOnSuccessListener(aVoid2 -> {
                                    showToast("Поканата е изпратена");
                                    if (onSuccess != null) onSuccess.run();
                                })
                                .addOnFailureListener(e -> showToast("Грешка при записване в получателя"))
                )
                .addOnFailureListener(e -> showToast("Грешка при записване в изпращача"));
    }

    public void acceptFriendRequest(String currentUid, String fromUid, Runnable onSuccess) {
        usersRef.child(currentUid).child("friendRequests").child("from").child(fromUid).removeValue();
        usersRef.child(fromUid).child("friendRequests").child("to").child(currentUid).removeValue();

        usersRef.child(currentUid).child("friends").child(fromUid).setValue(true);
        usersRef.child(fromUid).child("friends").child(currentUid).setValue(true);

        showToast("Добавихте се като приятели");
        if (onSuccess != null) onSuccess.run();
    }

    public void declineFriendRequest(String currentUid, String fromUid, Runnable onSuccess) {
        usersRef.child(currentUid).child("friendRequests").child("from").child(fromUid).removeValue();
        usersRef.child(fromUid).child("friendRequests").child("to").child(currentUid).removeValue();

        showToast("Поканата е отказана");
        if (onSuccess != null) onSuccess.run();
    }

    public void blockUser(String blockerUid, String blockedUid, Runnable onSuccess) {
        usersRef.child(blockerUid).child("blocked").child(blockedUid).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(blockerUid).child("friends").child(blockedUid).removeValue();
                    usersRef.child(blockedUid).child("friends").child(blockerUid).removeValue();

                    usersRef.child(blockerUid).child("friendRequests").child("to").child(blockedUid).removeValue();
                    usersRef.child(blockerUid).child("friendRequests").child("from").child(blockedUid).removeValue();
                    usersRef.child(blockedUid).child("friendRequests").child("to").child(blockerUid).removeValue();
                    usersRef.child(blockedUid).child("friendRequests").child("from").child(blockerUid).removeValue();

                    showToast("Потребителят е блокиран");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> showToast("Грешка при блокиране"));
    }

    public void unblockUser(String blockerUid, String blockedUid, Runnable onSuccess) {
        usersRef.child(blockerUid).child("blocked").child(blockedUid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    showToast("Потребителят е разблокиран");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> showToast("Грешка при разблокиране"));
    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
