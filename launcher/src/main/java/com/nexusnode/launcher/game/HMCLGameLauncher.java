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
package com.nexusnode.launcher.game;

import com.nexusnode.launcher.Metadata;
import com.nexusnode.launcher.auth.AuthInfo;
import com.nexusnode.launcher.launch.DefaultLauncher;
import com.nexusnode.launcher.launch.ProcessListener;

import java.util.Map;

/**
 * @author bluebird6900
 */
public final class HMCLGameLauncher extends DefaultLauncher {

    public HMCLGameLauncher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options) {
        this(repository, version, authInfo, options, null);
    }

    public HMCLGameLauncher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener) {
        this(repository, version, authInfo, options, listener, true);
    }

    public HMCLGameLauncher(GameRepository repository, Version version, AuthInfo authInfo, LaunchOptions options, ProcessListener listener, boolean daemon) {
        super(repository, version, authInfo, options, listener, daemon);
    }

    @Override
    protected Map<String, String> getConfigurations() {
        Map<String, String> res = super.getConfigurations();
        res.put("${launcher_name}", Metadata.NAME);
        res.put("${launcher_version}", Metadata.VERSION);
        return res;
    }
}
