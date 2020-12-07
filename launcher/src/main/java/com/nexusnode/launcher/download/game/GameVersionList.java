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

import com.nexusnode.launcher.download.DownloadProvider;
import com.nexusnode.launcher.download.VersionList;
import com.nexusnode.launcher.task.GetTask;
import com.nexusnode.launcher.task.OkHttpResponseFuture;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.util.gson.JsonUtils;
import com.nexusnode.launcher.util.io.NetworkUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.ea.async.Async.await;

/**
 *
 * @author bluebird6900
 */
public final class GameVersionList extends VersionList<GameRemoteVersion> {
    private final DownloadProvider downloadProvider;

    public GameVersionList(DownloadProvider downloadProvider) {
        this.downloadProvider = downloadProvider;
    }

    @Override
    public boolean hasType() {
        return true;
    }

    @Override
    protected Collection<GameRemoteVersion> getVersionsImpl(String gameVersion) {
        lock.readLock().lock();
        try {
            return versions.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public CompletableFuture<?> refreshFuture() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(downloadProvider.getVersionListURL())
                .build();

        Call call = client.newCall(request);
        OkHttpResponseFuture result = new OkHttpResponseFuture();
        call.enqueue(result);

        return result.future.thenComposeAsync(response -> {
            try {
                lock.writeLock().lock();
                versions.clear();

                String json = response.body().string();
                GameRemoteVersions root = JsonUtils.GSON.fromJson(json, GameRemoteVersions.class);
                for (GameRemoteVersionInfo remoteVersion : root.getVersions()) {
                    versions.put(remoteVersion.getGameVersion(), new GameRemoteVersion(
                            remoteVersion.getGameVersion(),
                            remoteVersion.getGameVersion(),
                            Collections.singletonList(remoteVersion.getUrl()),
                            remoteVersion.getType(), remoteVersion.getReleaseTime())
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }

            return CompletableFuture.completedFuture(null);
        })
        .exceptionally(throwable -> {
            throw new CompletionException(throwable);
        });
    }

    @Override
    public Task<?> refreshAsync() {
        GetTask task = new GetTask(NetworkUtils.toURL(downloadProvider.getVersionListURL()));
        return new Task<Void>() {
            @Override
            public Collection<Task<?>> getDependents() {
                return Collections.singleton(task);
            }

            @Override
            public void execute() {
                lock.writeLock().lock();

                try {
                    versions.clear();

                    GameRemoteVersions root = JsonUtils.GSON.fromJson(task.getResult(), GameRemoteVersions.class);
                    for (GameRemoteVersionInfo remoteVersion : root.getVersions()) {
                        versions.put(remoteVersion.getGameVersion(), new GameRemoteVersion(
                                remoteVersion.getGameVersion(),
                                remoteVersion.getGameVersion(),
                                Collections.singletonList(remoteVersion.getUrl()),
                                remoteVersion.getType(), remoteVersion.getReleaseTime())
                        );
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }
        };
    }
}
