package com.example.adventurecompass;

public class UserModel {
    private String name;
    private String email;
    private String bio;
    private String profilePictureUrl;

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
}
