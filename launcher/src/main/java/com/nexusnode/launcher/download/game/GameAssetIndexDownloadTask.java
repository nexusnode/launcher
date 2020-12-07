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

import com.nexusnode.launcher.download.AbstractDependencyManager;
import com.nexusnode.launcher.game.AssetIndexInfo;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.task.FileDownloadTask;
import com.nexusnode.launcher.task.Task;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This task is to download asset index file provided in minecraft.json.
 *
 * @author bluebird6900
 */
public final class GameAssetIndexDownloadTask extends Task<Void> {

    private final AbstractDependencyManager dependencyManager;
    private final Version version;
    private final List<Task<?>> dependencies = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param dependencyManager the dependency manager that can provides {@link com.nexusnode.launcher.game.GameRepository}
     * @param version the <b>resolved</b> version
     */
    public GameAssetIndexDownloadTask(AbstractDependencyManager dependencyManager, Version version) {
        this.dependencyManager = dependencyManager;
        this.version = version;
        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute() {
        AssetIndexInfo assetIndexInfo = version.getAssetIndex();
        File assetIndexFile = dependencyManager.getGameRepository().getIndexFile(version.getId(), assetIndexInfo.getId());

        // We should not check the hash code of asset index file since this file is not consistent
        // And Mojang will modify this file anytime. So assetIndex.hash might be outdated.
        dependencies.add(new FileDownloadTask(
                dependencyManager.getDownloadProvider().injectURLWithCandidates(assetIndexInfo.getUrl()),
                assetIndexFile
        ).setCacheRepository(dependencyManager.getCacheRepository()));
    }


    public static class GameAssetIndexMalformedException extends IOException {
    }
}
