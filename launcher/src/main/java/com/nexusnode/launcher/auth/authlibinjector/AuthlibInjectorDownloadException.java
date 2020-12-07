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
package com.nexusnode.launcher.auth.authlibinjector;

import com.nexusnode.launcher.auth.AuthenticationException;

/**
 * @author yushijinhun
 */
public class AuthlibInjectorDownloadException extends AuthenticationException {

    public AuthlibInjectorDownloadException() {
    }

    public AuthlibInjectorDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthlibInjectorDownloadException(String message) {
        super(message);
    }

    public AuthlibInjectorDownloadException(Throwable cause) {
        super(cause);
    }
}
