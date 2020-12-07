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
package com.nexusnode.launcher;

import java.nio.file.Path;

import com.nexusnode.launcher.util.io.JarUtils;
import com.nexusnode.launcher.util.platform.OperatingSystem;

/**
 * Stores metadata about this application.
 */
public final class Metadata {
    private Metadata() {}

    public static final String VERSION = System.getProperty("hmcl.version.override", JarUtils.thisJar().flatMap(JarUtils::getImplementationVersion).orElse("@develop@"));
    public static final String NAME = "HMCL";
    public static final String TITLE = NAME + " " + VERSION;
    
    public static final String UPDATE_URL = System.getProperty("hmcl.update_source.override", "https://hmcl.bluebird6900.net/api/update_link");
    public static final String CONTACT_URL = "https://hmcl.bluebird6900.net/contact";
    public static final String HELP_URL = "https://hmcl.bluebird6900.net/help";
    public static final String CHANGELOG_URL = "https://hmcl.bluebird6900.net/changelog/";
    public static final String PUBLISH_URL = "http://www.mcbbs.net/thread-142335-1-1.html";

    public static final Path MINECRAFT_DIRECTORY = OperatingSystem.getWorkingDirectory("minecraft");
    public static final Path HMCL_DIRECTORY = OperatingSystem.getWorkingDirectory("hmcl");
}