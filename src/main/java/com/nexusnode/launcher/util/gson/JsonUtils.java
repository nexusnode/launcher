package com.nexusnode.launcher.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonUtils {
    public static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();
}
