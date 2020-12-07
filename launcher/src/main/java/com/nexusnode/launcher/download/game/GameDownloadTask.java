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
package com.nexusnode.launcher.download.game;

import com.nexusnode.launcher.download.DefaultDependencyManager;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.task.FileDownloadTask;
import com.nexusnode.launcher.task.FileDownloadTask.IntegrityCheck;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.util.CacheRepository;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Task to download Minecraft jar
 * @author bluebird6900
 */
public final class GameDownloadTask extends Task<Void> {
    private final DefaultDependencyManager dependencyManager;
    private final String gameVersion;
    private final Version version;
    private final List<Task<?>> dependencies = new LinkedList<>();

    public GameDownloadTask(DefaultDependencyManager dependencyManager, String gameVersion, Version version) {
        this.dependencyManager = dependencyManager;
        this.gameVersion = gameVersion;
        this.version = version.resolve(dependencyManager.getGameRepository());

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        File jar = dependencyManager.getGameRepository().getVersionJar(version);

        FileDownloadTask task = new FileDownloadTask(
                dependencyManager.getDownloadProvider().injectURLWithCandidates(version.getDownloadInfo().getUrl()),
                jar,
                IntegrityCheck.of(CacheRepository.SHA1, version.getDownloadInfo().getSha1()))
                .setCaching(true)
                .setCacheRepository(dependencyManager.getCacheRepository());

        if (gameVersion != null)
            task.setCandidate(dependencyManager.getCacheRepository().getCommonDirectory().resolve("jars").resolve(gameVersion + ".jar"));

        dependencies.add(task);
    }
    
}
