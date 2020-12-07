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

import com.nexusnode.launcher.auth.AccountFactory;
import com.nexusnode.launcher.auth.AuthenticationException;
import com.nexusnode.launcher.auth.CharacterSelector;
import com.nexusnode.launcher.auth.yggdrasil.CompleteGameProfile;
import com.nexusnode.launcher.auth.yggdrasil.GameProfile;
import com.nexusnode.launcher.auth.yggdrasil.YggdrasilSession;
import com.nexusnode.launcher.util.javafx.ObservableOptionalCache;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.nexusnode.launcher.util.Lang.tryCast;

public class AuthlibInjectorAccountFactory extends AccountFactory<AuthlibInjectorAccount> {
    private AuthlibInjectorArtifactProvider downloader;
    private Function<String, AuthlibInjectorServer> serverLookup;

    /**
     * @param serverLookup a function that looks up {@link AuthlibInjectorServer} by url
     */
    public AuthlibInjectorAccountFactory(AuthlibInjectorArtifactProvider downloader, Function<String, AuthlibInjectorServer> serverLookup) {
        this.downloader = downloader;
        this.serverLookup = serverLookup;
    }

    @Override
    public AuthlibInjectorAccount create(CharacterSelector selector, String username, String password, Object additionalData) throws AuthenticationException {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        AuthlibInjectorServer server = (AuthlibInjectorServer) additionalData;

        return new AuthlibInjectorAccount(server, downloader, username, password, selector);
    }

    @Override
    public AuthlibInjectorAccount fromStorage(Map<Object, Object> storage) {
        Objects.requireNonNull(storage);

        YggdrasilSession session = YggdrasilSession.fromStorage(storage);

        String username = tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have username"));
        String apiRoot = tryCast(storage.get("serverBaseURL"), String.class)
                .orElseThrow(() -> new IllegalArgumentException("storage does not have API root."));

        AuthlibInjectorServer server = serverLookup.apply(apiRoot);

        tryCast(storage.get("profileProperties"), Map.class).ifPresent(
                it -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> properties = it;
                    GameProfile selected = session.getSelectedProfile();
                    ObservableOptionalCache<UUID, CompleteGameProfile, AuthenticationException> profileRepository = server.getYggdrasilService().getProfileRepository();
                    profileRepository.put(selected.getId(), new CompleteGameProfile(selected, properties));
                    profileRepository.invalidate(selected.getId());
                });

        return new AuthlibInjectorAccount(server, downloader, username, session);
    }
}
