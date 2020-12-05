package com.nexusnode.launcher;

public class AuthService {
    private static final String AUTH_SERVER = "https://authserver.mojang.com/";

    public AuthResponse requestLogin(String username, String password, String clientToken) {
        AuthRequest request = new AuthRequest(username, password, clientToken);
        String data = MojangUtils.getGson().toJson(request);

        AuthResponse response;
        try {
            String returned = postJson(AUTH_SERVER + "authenticate", data);
            response = MojangUtils.getGson().fromJson(returned, AuthResponse.class);
        } catch (IOException e) {
            throw new AuthenticationNetworkFailureException("authserver.mojang.com", e);
        }
        return response;
    }
}
