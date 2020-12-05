package com.nexusnode.launcher.minecraft.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthService {
    private static final String AUTH_SERVER = "https://authserver.mojang.com/";

    public AuthResponse requestLogin(String username, String password, String clientToken) throws IOException {
        AuthRequest request = new AuthRequest(username, password, clientToken);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String data = gson.toJson(request);

        AuthResponse response = null;
        try {
            String returned = sendPostJson(AUTH_SERVER + "authenticate", data);
            response = gson.fromJson(returned, AuthResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private String sendPostJson(String url, String data) throws IOException {
        OkHttpClient client = new OkHttpClient();
        byte[] rawData = data.getBytes(StandardCharsets.UTF_8);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder().url(url).post(body).build();

        Request.Builder builder = request.newBuilder();
        builder.header("Content-Length", Integer.toString(rawData.length));
        builder.header("Content-Language", "en-US");

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
