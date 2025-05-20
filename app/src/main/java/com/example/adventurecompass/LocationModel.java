package com.example.adventurecompass;

import java.io.Serializable;

public class LocationModel  implements Serializable {
    private String id,name,description, imageUrl;

    public LocationModel(){}
    public LocationModel(String id,String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }
    public LocationModel(String locationName) {
        this.name = locationName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
