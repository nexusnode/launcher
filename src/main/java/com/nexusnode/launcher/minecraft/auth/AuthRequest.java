package com.nexusnode.launcher.minecraft.auth;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class AuthRequest {
    private final Agent agent;
    private final String username;
    private final String password;
    private final String clientToken;
    private final boolean requestUser = true;

    public AuthRequest(String username, String password, String clientToken) {
        this.agent = new Agent();
        this.username = username;
        this.password = password;
        this.clientToken = clientToken;
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    public static class Agent {
        private final String name = "Minecraft";
        private final int version = 1;
    }
}

