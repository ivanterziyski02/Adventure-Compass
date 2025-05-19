package com.example.adventurecompass.chats;

public class MessageModel {
    private String senderId;
    private String receiverId;
    private String text;
    private long timestamp;

    public MessageModel() {}

    public MessageModel(String senderId, String receiverId, String text, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }

    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
