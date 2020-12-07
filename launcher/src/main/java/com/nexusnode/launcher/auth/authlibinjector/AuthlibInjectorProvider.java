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
import com.nexusnode.launcher.auth.yggdrasil.YggdrasilProvider;
import com.nexusnode.launcher.util.gson.UUIDTypeAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class AuthlibInjectorProvider implements YggdrasilProvider {

    private final String apiRoot;

    public AuthlibInjectorProvider(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public URL getAuthenticationURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/authenticate");
    }

    @Override
    public URL getRefreshmentURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/refresh");
    }

    @Override
    public URL getValidationURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/validate");
    }

    @Override
    public URL getInvalidationURL() throws AuthenticationException {
        return toURL(apiRoot + "authserver/invalidate");
    }

    @Override
    public URL getProfilePropertiesURL(UUID uuid) throws AuthenticationException {
        return toURL(apiRoot + "sessionserver/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(uuid));
    }

    @Override
    public String toString() {
        return apiRoot;
    }

    private URL toURL(String url) throws AuthenticationException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AuthenticationException(e);
        }
    }
}