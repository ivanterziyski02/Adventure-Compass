package com.example.adventurecompass.chats;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatManager {
    private final DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    public interface ChatIdCallback {
        void onResult(String chatId);
    }

    public void getOrCreateChatId(String uid1, String uid2, ChatIdCallback callback) {
        String chatId = uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
        callback.onResult(chatId);
    }

    public void sendMessage(String senderId, String receiverId, String text) {
        getOrCreateChatId(senderId, receiverId, chatId -> {
            long timestamp = System.currentTimeMillis();
            String messageId = db.child("messages").child(chatId).push().getKey();

            MessageModel message = new MessageModel(senderId, receiverId, text, timestamp);

            Map<String, Object> updates = new HashMap<>();

            updates.put("/messages/" + chatId + "/" + messageId, message);

            Map<String, Object> chatData = new HashMap<>();
            chatData.put("participants", Arrays.asList(senderId, receiverId));
            chatData.put("lastMessage", text);
            chatData.put("lastTimestamp", timestamp);
            updates.put("/chats/" + chatId, chatData);

            Map<String, Object> userChat1 = new HashMap<>();
            userChat1.put("with", receiverId);
            userChat1.put("lastMessage", text);
            userChat1.put("timestamp", timestamp);

            Map<String, Object> userChat2 = new HashMap<>();
            userChat2.put("with", senderId);
            userChat2.put("lastMessage", text);
            userChat2.put("timestamp", timestamp);

            updates.put("/userChats/" + senderId + "/" + chatId, userChat1);
            updates.put("/userChats/" + receiverId + "/" + chatId, userChat2);

            db.updateChildren(updates);

        });
    }
}
