/*
 * This file is part of Technic Minecraft Core.
 * Copyright ©2015 Syndicate, LLC
 *
 * Technic Minecraft Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Minecraft Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * as well as a copy of the GNU Lesser General Public License,
 * along with Technic Minecraft Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.technicpack.minecraftcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.technicpack.minecraftcore.mojang.auth.io.UserProperties;
import net.technicpack.minecraftcore.mojang.auth.io.UserPropertiesAdapter;
import net.technicpack.minecraftcore.mojang.version.MojangVersion;
import net.technicpack.minecraftcore.mojang.version.io.CompleteVersion;
import net.technicpack.minecraftcore.mojang.version.io.CompleteVersionV21;
import net.technicpack.minecraftcore.mojang.version.io.Rule;
import net.technicpack.minecraftcore.mojang.version.io.RuleAdapter;
import net.technicpack.minecraftcore.mojang.version.io.argument.ArgumentList;
import net.technicpack.minecraftcore.mojang.version.io.argument.ArgumentListAdapter;
import net.technicpack.utilslib.DateTypeAdapter;
import net.technicpack.utilslib.LowerCaseEnumTypeAdapterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MojangUtils {
    public static final String baseURL = "https://s3.amazonaws.com/Minecraft.Download/";
    public static final String assetsIndexes = baseURL + "indexes/";
    public static final String versions = baseURL + "versions/";
    public static final String assets = "http://resources.download.minecraft.net/";
    public static final String versionList = versions + "versions.json";

    public static String getVersionJson(String version) {
        return versions + version + "/" + version + ".json";
    }

    public static String getVersionDownload(String version) {
        return versions + version + "/" + version + ".jar";
    }

    public static String getAssetsIndex(String assetsKey) {
        return assetsIndexes + assetsKey + ".json";
    }

    public static String getResourceUrl(String hash) {
        return assets + hash.substring(0, 2) + "/" + hash;
    }

    private static final Gson gson;
    private static final Gson uglyGson;
    private static final NavigableMap<Integer, Class<? extends MojangVersion>> versionJsonVersions;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
        builder.registerTypeAdapter(UserProperties.class, new UserPropertiesAdapter());
        builder.registerTypeAdapter(ArgumentList.class, new ArgumentListAdapter());
        builder.registerTypeAdapter(Rule.class, new RuleAdapter());
        builder.enableComplexMapKeySerialization();
        uglyGson = builder.create();

        builder.setPrettyPrinting();
        gson = builder.create();

        versionJsonVersions = new TreeMap<Integer, Class<? extends MojangVersion>>();
        versionJsonVersions.put(0, CompleteVersion.class);
        versionJsonVersions.put(21, CompleteVersionV21.class);
    }

    public static Gson getGson() {
        return gson;
    }

    public static Gson getUglyGson() {
        return uglyGson;
    }

    public static void copyMinecraftJar(File minecraft, File output) throws IOException {
        String[] security = { "MOJANG_C.DSA",
                "MOJANG_C.SF",
                "CODESIGN.RSA",
                "CODESIGN.SF" };
        JarFile jarFile = new JarFile(minecraft);
        try {
            String fileName = jarFile.getName();
            String fileNameLastPart = fileName.substring(fileName.lastIndexOf(File.separator));

            JarOutputStream jos = new JarOutputStream(new FileOutputStream(output));
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (containsAny(entry.getName(), security)) {
                    continue;
                }
                InputStream is = jarFile.getInputStream(entry);

                //jos.putNextEntry(entry);
                //create a new entry to avoid ZipException: invalid entry compressed size
                jos.putNextEntry(new JarEntry(entry.getName()));
                byte[] buffer = new byte[4096];
                int bytesRead = 0;
                while ((bytesRead = is.read(buffer)) != -1) {
                    jos.write(buffer, 0, bytesRead);
                }
                is.close();
                jos.flush();
                jos.closeEntry();
            }
            jos.close();
        } finally {
            jarFile.close();
        }

    }

    private static boolean containsAny(String inputString, String[] contains) {
        for (String string : contains) {
            if (inputString.contains(string)) {
                return true;
            }
        }
        return false;
    }

    public static MojangVersion parseVersionJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Class<? extends MojangVersion> versionJsonType;
        if (root.has("minimumLauncherVersion")) {
            int minLauncherVersion = root.get("minimumLauncherVersion").getAsInt();
            Map.Entry<Integer, Class<? extends MojangVersion>> entry = versionJsonVersions.floorEntry(minLauncherVersion);
            if (entry == null) {
                throw new IllegalArgumentException("Unsupported minimumLauncherVersion: " + minLauncherVersion);
            }
            versionJsonType = entry.getValue();
        } else { // fallback: check if "arguments" key exists since only 1.13+ should have it
            versionJsonType = root.has("arguments") ? CompleteVersionV21.class : CompleteVersion.class;
        }
        return getGson().fromJson(root, versionJsonType);
    }

    public static boolean isLegacyVersion(String version) {
        final String[] versionParts = version.split("[.-]", 3);

        return Integer.valueOf(versionParts[0]) == 1 && Integer.valueOf(versionParts[1]) < 6;
    }
}
