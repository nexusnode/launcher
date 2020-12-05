package com.nexusnode.launcher.minecraft.models;


@SuppressWarnings({"unused"})
public class Profile {
    private String id;
    private String name;
    private boolean legacy;

    public Profile() {

    }

    public Profile(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLegacy() {
        return legacy;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

