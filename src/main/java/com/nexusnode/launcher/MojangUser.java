package com.nexusnode.launcher;

import com.nexusnode.launcher.minecraft.auth.AuthResponse;
import com.nexusnode.launcher.minecraft.models.Profile;
import com.nexusnode.launcher.minecraft.models.UserProperties;

public class MojangUser implements IUserType {
    private String username;
    private String accessToken;
    private String clientToken;
    private String displayName;
    private Profile profile;
    private UserProperties userProperties;
    private transient boolean isOffline;

    public MojangUser() {
        isOffline = false;
    }

    //This constructor is used to build a user for offline mode
    public MojangUser(String username) {
        this.username = username;
        this.displayName = username;
        this.accessToken = "0";
        this.clientToken = "0";
        this.profile = new Profile("0", "");
        this.isOffline = true;
        this.userProperties = new UserProperties();
    }

    public MojangUser(String username, AuthResponse response) {
        this.isOffline = false;
        this.username = username;
        this.accessToken = response.getAccessToken();
        this.clientToken = response.getClientToken();
        this.displayName = response.getSelectedProfile().getName();
        this.profile = response.getSelectedProfile();

        if (response.getUser() == null) {
            this.userProperties = new UserProperties();
        } else {
            this.userProperties = response.getUser().getUserProperties();
        }
    }

    public String getId() {
        return profile.getId();
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Profile getProfile() {
        return profile;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public String getSessionId() {
        return "token:" + accessToken + ":" + profile.getId();
    }

    public void rotateAccessToken(String newToken) {
        this.accessToken = newToken;
    }

    public String getUserPropertiesAsJson() {
        /*if (this.userProperties != null) {
            return MojangUtils.getUglyGson().toJson(this.userProperties);
        }
        else*/ {
            return "{}";
        }
    }

    public void mergeUserProperties(MojangUser mergeUser) {
        if (this.userProperties != null && mergeUser.userProperties != null)
            this.userProperties.merge(mergeUser.userProperties);
    }
}
