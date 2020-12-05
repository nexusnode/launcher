package com.nexusnode.launcher.minecraft.models;

@SuppressWarnings({"unused"})
public class User {
    private String id;
    private UserProperties properties;

    public User() {

    }

    public String getId() {
        return id;
    }

    public UserProperties getUserProperties() {
        return properties;
    }
}

