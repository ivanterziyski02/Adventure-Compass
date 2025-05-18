package com.example.adventurecompass.chats;

public class ChatItemModel {
    private String with;
    private String lastMessage;
    private long timestamp;

    public ChatItemModel() {}

    public ChatItemModel(String with, String lastMessage, long timestamp) {
        this.with = with;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
