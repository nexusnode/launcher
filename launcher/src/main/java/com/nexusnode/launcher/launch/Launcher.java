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
package com.nexusnode.launcher.launch;

import com.nexusnode.launcher.auth.AuthInfo;
import com.nexusnode.launcher.game.GameRepository;
import com.nexusnode.launcher.game.LaunchOptions;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.util.platform.ManagedProcess;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author bluebird6900
 */
public abstract class Launcher {

    protected final GameRepository repository;
    protected final Version version;
    protected final AuthInfo authInfo;
    protected final LaunchOptions options;
    protected final ProcessListener listener;
    protected final boolean daemon;

    public Launcher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options) {
        this(repository, version, authInfo, options, null);
    }

    public Launcher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener) {
        this(repository, version, authInfo, options, listener, true);
    }

    public Launcher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener, boolean daemon) {
        this.repository = repository;
        this.version = version;
        this.authInfo = authInfo;
        this.options = options;
        this.listener = listener;
        this.daemon = daemon;
    }

    /**
     * @param file the file path.
     */
    public abstract void makeLaunchScript(File file) throws IOException;

    public abstract ManagedProcess launch() throws IOException, InterruptedException;

}
