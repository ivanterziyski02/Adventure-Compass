package com.example.adventurecompass;

public class UserModel {
    private String name;
    private String email;
    private String bio;
    private String profilePictureUrl;

    public UserModel() {
        // Нужен за Firebase
    }

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
}
