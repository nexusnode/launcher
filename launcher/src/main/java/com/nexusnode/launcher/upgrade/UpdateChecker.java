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
package com.nexusnode.launcher.upgrade;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import com.nexusnode.launcher.Metadata;
import com.nexusnode.launcher.setting.ConfigHolder;
import com.nexusnode.launcher.util.io.NetworkUtils;

import java.io.IOException;
import java.util.logging.Level;

import static com.nexusnode.launcher.setting.ConfigHolder.config;
import static com.nexusnode.launcher.ui.FXUtils.onInvalidating;
import static com.nexusnode.launcher.util.Lang.mapOf;
import static com.nexusnode.launcher.util.Lang.thread;
import static com.nexusnode.launcher.util.Logging.LOG;
import static com.nexusnode.launcher.util.Pair.pair;
import static com.nexusnode.launcher.util.versioning.VersionNumber.asVersion;

public final class UpdateChecker {
    private UpdateChecker() {}

    private static ObjectProperty<RemoteVersion> latestVersion = new SimpleObjectProperty<>();
    private static BooleanBinding outdated = Bindings.createBooleanBinding(
            () -> {
                RemoteVersion latest = latestVersion.get();
                if (latest == null || isDevelopmentVersion(Metadata.VERSION)) {
                    return false;
                } else {
                    return asVersion(latest.getVersion()).compareTo(asVersion(Metadata.VERSION)) > 0;
                }
            },
            latestVersion);
    private static ReadOnlyBooleanWrapper checkingUpdate = new ReadOnlyBooleanWrapper(false);

    public static void init() {
        ConfigHolder.config().updateChannelProperty().addListener(onInvalidating(UpdateChecker::requestCheckUpdate));
        requestCheckUpdate();
    }

    public static RemoteVersion getLatestVersion() {
        return latestVersion.get();
    }

    public static ReadOnlyObjectProperty<RemoteVersion> latestVersionProperty() {
        return latestVersion;
    }

    public static boolean isOutdated() {
        return outdated.get();
    }

    public static ObservableBooleanValue outdatedProperty() {
        return outdated;
    }

    public static boolean isCheckingUpdate() {
        return checkingUpdate.get();
    }

    public static ReadOnlyBooleanProperty checkingUpdateProperty() {
        return checkingUpdate.getReadOnlyProperty();
    }

    private static RemoteVersion checkUpdate(UpdateChannel channel) throws IOException {
        if (!IntegrityChecker.isSelfVerified() && !"true".equals(System.getProperty("hmcl.self_integrity_check.disable"))) {
            throw new IOException("Self verification failed");
        }

        String url = NetworkUtils.withQuery(Metadata.UPDATE_URL, mapOf(
                pair("version", Metadata.VERSION),
                pair("channel", channel.channelName)));

        return RemoteVersion.fetch(url);
    }

    private static boolean isDevelopmentVersion(String version) {
        return version.contains("@") || // eg. @develop@
                version.contains("SNAPSHOT"); // eg. 3.1.SNAPSHOT
    }

    public static void requestCheckUpdate() {
        Platform.runLater(() -> {
            if (isCheckingUpdate())
                return;
            checkingUpdate.set(true);
            UpdateChannel channel = config().getUpdateChannel();

            thread(() -> {
                RemoteVersion result = null;
                try {
                    result = checkUpdate(channel);
                    LOG.info("Latest version (" + channel + ") is " + result);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Failed to check for update", e);
                }

                RemoteVersion finalResult = result;
                Platform.runLater(() -> {
                    checkingUpdate.set(false);
                    if (finalResult != null) {
                        if (channel.equals(config().getUpdateChannel())) {
                            latestVersion.set(finalResult);
                        } else {
                            // the channel has been changed during the period
                            // check update again
                            requestCheckUpdate();
                        }
                    }
                });
            }, "Update Checker", true);
        });
    }
}
