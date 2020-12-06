package com.nexusnode.launcher;

import com.nexusnode.launcher.download.game.GameRemoteVersions;
import com.nexusnode.launcher.util.gson.JsonUtils;
import okhttp3.*;

import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class Start {
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launch(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://launchermeta.mojang.com/mc/game/version_manifest.json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                System.out.println(result);

                GameRemoteVersions root = JsonUtils.GSON.fromJson(result, GameRemoteVersions.class);
            }

            public void onFailure(Call call, IOException e) {

            }
        });
    }
}
