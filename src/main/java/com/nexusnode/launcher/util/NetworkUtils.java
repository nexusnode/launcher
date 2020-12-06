package com.nexusnode.launcher.util;

import java.net.*;

public final class NetworkUtils {
    public static URL toURL(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
