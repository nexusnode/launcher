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
package com.nexusnode.launcher.auth.offline;

import com.nexusnode.launcher.auth.AccountFactory;
import com.nexusnode.launcher.auth.CharacterSelector;
import com.nexusnode.launcher.util.gson.UUIDTypeAdapter;

import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.nexusnode.launcher.util.Lang.tryCast;

/**
 *
 * @author bluebird6900
 */
public class OfflineAccountFactory extends AccountFactory<OfflineAccount> {
    public static final OfflineAccountFactory INSTANCE = new OfflineAccountFactory();

    private OfflineAccountFactory() {
    }

    public OfflineAccount create(String username, UUID uuid) {
        return new OfflineAccount(username, uuid);
    }

    @Override
    public OfflineAccount create(CharacterSelector selector, String username, String password, Object additionalData) {
        return new OfflineAccount(username, getUUIDFromUserName(username));
    }

    @Override
    public OfflineAccount fromStorage(Map<Object, Object> storage) {
        String username = tryCast(storage.get("username"), String.class)
                .orElseThrow(() -> new IllegalStateException("Offline account configuration malformed."));
        UUID uuid = tryCast(storage.get("uuid"), String.class)
                .map(UUIDTypeAdapter::fromString)
                .orElse(getUUIDFromUserName(username));

        return new OfflineAccount(username, uuid);
    }

    private static UUID getUUIDFromUserName(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(UTF_8));
    }

}
