package com.example.adventurecompass;

public class ReviewModel {
    String userId, userName, description, url;

    ReviewModel()
    {}
    public ReviewModel(String userId,String userName,String description, String url) {
        this.userId = userId;
        this.userName = userName;
        this.description = description;
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
