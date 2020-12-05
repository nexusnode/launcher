package com.nexusnode.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftLauncher {
    private String javaVersions = "C:\\Program Files\\Java\\jdk1.8.0_271";

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launch(String[] args) throws Exception {
        String directory = "";
        ProcessBuilder processBuilder = new ProcessBuilder(commands).directory(directory).redirectErrorStream(true);

        Process process = processBuilder.start();
    }

    private List<String> buildCommands(ModpackModel pack, long memory, MojangVersion version, LaunchOptions options) {
        List<String> commands = new ArrayList<>();

        Map<String, String> params = new HashMap<String, String>();

        params.put("auth_username", mojangUser.getUsername());
        params.put("auth_session", mojangUser.getSessionId());
        params.put("auth_access_token", mojangUser.getAccessToken());

        params.put("auth_player_name", mojangUser.getDisplayName());
        params.put("auth_uuid", mojangUser.getProfile().getId());

        params.put("profile_name", mojangUser.getDisplayName());
        params.put("version_name", version.getId());
        params.put("version_type", version.getType().getName());

        params.put("game_directory", gameDirectory.getAbsolutePath());
        params.put("natives_directory", nativesDir);
        params.put("classpath", cpString);

        params.put("resolution_width", Integer.toString(launchOpts.getCustomWidth()));
        params.put("resolution_height", Integer.toString(launchOpts.getCustomHeight()));

        return commands;
    }
}
