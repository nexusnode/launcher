/*
 * Crafting Dead Launcher
 * Copyright (C) 2020  bluebird6900  and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.nexusnode.launcher.util.gson;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * @author yushijinhun
 */
public final class JsonUtils {

    public static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .registerTypeAdapter(Date.class, DateTypeAdapter.INSTANCE)
            .registerTypeAdapter(UUID.class, UUIDTypeAdapter.INSTANCE)
            .registerTypeAdapter(File.class, FileTypeAdapter.INSTANCE)
            .registerTypeAdapterFactory(ValidationTypeAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(LowerCaseEnumTypeAdapterFactory.INSTANCE)
            .create();

    private JsonUtils() {
    }

    public static <T> T fromNonNullJson(String json, Class<T> classOfT) throws JsonParseException {
        T parsed = GSON.fromJson(json, classOfT);
        if (parsed == null)
            throw new JsonParseException("Json object cannot be null.");
        return parsed;
    }

    public static <T> T fromMaybeMalformedJson(String json, Class<T> classOfT) throws JsonParseException {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
