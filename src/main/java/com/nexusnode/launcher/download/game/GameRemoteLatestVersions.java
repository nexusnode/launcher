package com.nexusnode.launcher.download.game;

import com.google.gson.annotations.SerializedName;

//@Immutable -- TODO
public final class GameRemoteLatestVersions {

    @SerializedName("snapshot")
    private final String snapshot;

    @SerializedName("release")
    private final String release;

    public GameRemoteLatestVersions() {
        this(null, null);
    }

    public GameRemoteLatestVersions(String snapshot, String release) {
        this.snapshot = snapshot;
        this.release = release;
    }

    public String getRelease() {
        return release;
    }

    public String getSnapshot() {
        return snapshot;
    }
}

