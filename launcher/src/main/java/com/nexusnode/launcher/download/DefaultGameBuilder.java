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
package com.nexusnode.launcher.download;

import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.util.function.ExceptionalFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bluebird6900
 */
public class DefaultGameBuilder extends GameBuilder {

    private final DefaultDependencyManager dependencyManager;

    public DefaultGameBuilder(DefaultDependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    public DefaultDependencyManager getDependencyManager() {
        return dependencyManager;
    }

    @Override
    public Task<?> buildAsync() {
        List<String> stages = new ArrayList<>();

        Task<Version> libraryTask = Task.supplyAsync(() -> new Version(name));
        libraryTask = libraryTask.thenComposeAsync(libraryTaskHelper(gameVersion, "game", gameVersion));
        stages.add("hmcl.install.game:" + gameVersion);
        stages.add("hmcl.install.assets");

        for (Map.Entry<String, String> entry : toolVersions.entrySet()) {
            libraryTask = libraryTask.thenComposeAsync(libraryTaskHelper(gameVersion, entry.getKey(), entry.getValue()));
            stages.add(String.format("hmcl.install.%s:%s", entry.getKey(), entry.getValue()));
        }

        for (RemoteVersion remoteVersion : remoteVersions) {
            libraryTask = libraryTask.thenComposeAsync(version -> dependencyManager.installLibraryAsync(version, remoteVersion));
            stages.add(String.format("hmcl.install.%s:%s", remoteVersion.getLibraryId(), remoteVersion.getSelfVersion()));
        }

        return libraryTask.thenComposeAsync(dependencyManager.getGameRepository()::save).whenComplete(exception -> {
            if (exception != null)
                dependencyManager.getGameRepository().removeVersionFromDisk(name);
        }).withStagesHint(stages);
    }

    private ExceptionalFunction<Version, Task<Version>, ?> libraryTaskHelper(String gameVersion, String libraryId, String libraryVersion) {
        return version -> dependencyManager.installLibraryAsync(gameVersion, version, libraryId, libraryVersion);
    }
}
