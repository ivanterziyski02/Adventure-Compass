package com.example.adventurecompass;

import java.util.Map;

public class UserModel {
    private String name;
    private String email;
    private String bio;
    private String profilePictureUrl;
    private long registrationDate;
    private String fcmToken;
    private Map<String, Map<String, Boolean>> friendRequests;;
    private Map<String, Boolean> blocked;
    private Map<String, Boolean> friends;

    public UserModel() {}

    public UserModel(String name, String email, String bio, String profilePictureUrl) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public long getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(long registrationDate) { this.registrationDate = registrationDate; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public Map<String, Map<String, Boolean>> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(Map<String, Map<String, Boolean>> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public Map<String, Boolean> getBlocked() { return blocked; }
    public void setBlocked(Map<String, Boolean> blocked) { this.blocked = blocked; }


    public Map<String, Boolean> getFriends() { return friends; }
    public void setFriends(Map<String, Boolean> friends) { this.friends = friends; }
}
