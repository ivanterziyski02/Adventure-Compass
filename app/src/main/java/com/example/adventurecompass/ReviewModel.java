package com.example.adventurecompass;

import android.os.Parcel;
import android.os.Parcelable;

public class ReviewModel implements Parcelable {
    private String id, userId, userName, description, locationImageUrl, profilePictureUrl;

    public ReviewModel() {}

    public ReviewModel(String id, String userId, String userName, String description, String locationImageUrl, String profilePictureUrl) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.description = description;
        this.locationImageUrl = locationImageUrl;
        this.profilePictureUrl = profilePictureUrl;
    }

    protected ReviewModel(Parcel in) {
        id = in.readString();
        userId = in.readString();
        userName = in.readString();
        description = in.readString();
        locationImageUrl = in.readString();
        profilePictureUrl = in.readString();
    }

    public static final Creator<ReviewModel> CREATOR = new Creator<ReviewModel>() {
        @Override
        public ReviewModel createFromParcel(Parcel in) {
            return new ReviewModel(in);
        }

        @Override
        public ReviewModel[] newArray(int size) {
            return new ReviewModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(description);
        dest.writeString(locationImageUrl);
        dest.writeString(profilePictureUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Гетъри и сетъри

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationImageUrl() { return locationImageUrl; }
    public void setLocationImageUrl(String locationImageUrl) { this.locationImageUrl = locationImageUrl; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setUserProfilePictureUrl(String userProfilePictureUrl) { this.profilePictureUrl = userProfilePictureUrl; }
}
