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

import com.nexusnode.launcher.download.game.GameAssetDownloadTask;
import com.nexusnode.launcher.download.game.GameDownloadTask;
import com.nexusnode.launcher.download.game.GameLibrariesTask;
import com.nexusnode.launcher.game.DefaultGameRepository;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.task.Task;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Note: This class has no state.
 *
 * @author bluebird6900
 */
public class DefaultDependencyManager extends AbstractDependencyManager {

    private final DefaultGameRepository repository;
    private final DownloadProvider downloadProvider;
    private final DefaultCacheRepository cacheRepository;

    public DefaultDependencyManager(DefaultGameRepository repository, DownloadProvider downloadProvider, DefaultCacheRepository cacheRepository) {
        this.repository = repository;
        this.downloadProvider = downloadProvider;
        this.cacheRepository = cacheRepository;
    }

    @Override
    public DefaultGameRepository getGameRepository() {
        return repository;
    }

    @Override
    public DownloadProvider getDownloadProvider() {
        return downloadProvider;
    }

    @Override
    public DefaultCacheRepository getCacheRepository() {
        return cacheRepository;
    }

    @Override
    public GameBuilder gameBuilder() {
        return new DefaultGameBuilder(this);
    }

    @Override
    public Task<?> checkGameCompletionAsync(Version original, boolean integrityCheck) {
        Version version = original.resolve(repository);
        return Task.allOf(
                Task.composeAsync(() -> {
                    if (!repository.getVersionJar(version).exists())
                        return new GameDownloadTask(this, null, version);
                    else
                        return null;
                }),
                new GameAssetDownloadTask(this, version, GameAssetDownloadTask.DOWNLOAD_INDEX_IF_NECESSARY, integrityCheck),
                new GameLibrariesTask(this, version, integrityCheck)
        );
    }

    @Override
    public Task<?> checkLibraryCompletionAsync(Version version, boolean integrityCheck) {
        return new GameLibrariesTask(this, version, integrityCheck, version.getLibraries());
    }

    @Override
    public Task<Version> installLibraryAsync(String gameVersion, Version baseVersion, String libraryId, String libraryVersion) {
        if (baseVersion.isResolved()) throw new IllegalArgumentException("Version should not be resolved");

        VersionList<?> versionList = getVersionList(libraryId);
        return versionList.loadAsync(gameVersion)
                .thenComposeAsync(() -> installLibraryAsync(baseVersion, versionList.getVersion(gameVersion, libraryVersion)
                        .orElseThrow(() -> new IOException("Remote library " + libraryId + " has no version " + libraryVersion))))
                .withStage(String.format("hmcl.install.%s:%s", libraryId, libraryVersion));
    }

    @Override
    public Task<Version> installLibraryAsync(Version baseVersion, RemoteVersion libraryVersion) {
        if (baseVersion.isResolved()) throw new IllegalArgumentException("Version should not be resolved");

        return removeLibraryAsync(baseVersion.resolvePreservingPatches(repository), libraryVersion.getLibraryId())
                .thenComposeAsync(version -> libraryVersion.getInstallTask(this, version))
                .thenApplyAsync(baseVersion::addPatch)
                .withStage(String.format("hmcl.install.%s:%s", libraryVersion.getLibraryId(), libraryVersion.getSelfVersion()));
    }

    public Task<Version> installLibraryAsync(Version oldVersion, Path installer) {
        throw new NullPointerException(); //TODO - bluebird
    }

    public static class UnsupportedLibraryInstallerException extends Exception {
    }

    /**
     * Remove installed library.
     * Will try to remove libraries and patches.
     *
     * @param version not resolved version
     * @param libraryId forge/liteloader/optifine/fabric
     * @return task to remove the specified library
     */
    public Task<Version> removeLibraryAsync(Version version, String libraryId) {
        // MaintainTask requires version that does not inherits from any version.
        // If we want to remove a library in dependent version, we should keep the dependents not changed
        // So resolving this game version to preserve all information in this version.json is necessary.
        if (version.isResolved())
            throw new IllegalArgumentException("removeLibraryWithoutSavingAsync requires non-resolved version");
        Version independentVersion = version.resolvePreservingPatches(repository);

        return Task.supplyAsync(() -> LibraryAnalyzer.analyze(independentVersion).removeLibrary(libraryId).build());
    }

}
