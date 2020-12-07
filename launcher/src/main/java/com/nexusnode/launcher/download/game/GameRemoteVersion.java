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
import com.nexusnode.launcher.download.LibraryAnalyzer;
import com.nexusnode.launcher.download.RemoteVersion;
import com.nexusnode.launcher.game.ReleaseType;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.util.Immutable;

import java.util.Date;
import java.util.List;

/**
 *
 * @author bluebird6900
 */
@Immutable
public final class GameRemoteVersion extends RemoteVersion {

    private final ReleaseType type;
    private final Date time;

    public GameRemoteVersion(String gameVersion, String selfVersion, List<String> url, ReleaseType type, Date time) {
        super(LibraryAnalyzer.LibraryType.MINECRAFT.getPatchId(), gameVersion, selfVersion, getReleaseType(type), url);
        this.type = type;
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public ReleaseType getType() {
        return type;
    }

    @Override
    public Task<Version> getInstallTask(DefaultDependencyManager dependencyManager, Version baseVersion) {
        return new GameInstallTask(dependencyManager, baseVersion, this);
    }

    @Override
    public int compareTo(RemoteVersion o) {
        if (!(o instanceof GameRemoteVersion))
            return 0;

        return ((GameRemoteVersion) o).getTime().compareTo(getTime());
    }

    private static Type getReleaseType(ReleaseType type) {
        if (type == null) return Type.UNCATEGORIZED;
        switch (type) {
            case RELEASE:
                return Type.RELEASE;
            case SNAPSHOT:
                return Type.SNAPSHOT;
            case UNKNOWN:
                return Type.UNCATEGORIZED;
            default:
                return Type.OLD;
        }
    }
}
