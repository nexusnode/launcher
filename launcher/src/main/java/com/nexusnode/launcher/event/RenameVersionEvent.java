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
package com.nexusnode.launcher.event;

import com.nexusnode.launcher.util.ToStringBuilder;

/**
 * This event gets fired when a minecraft version is being removed.
 * <br>
 * This event is fired on the {@link com.nexusnode.launcher.event.EventBus#EVENT_BUS}
 *
 * @author bluebird6900
 */
public class RenameVersionEvent extends Event {

    private final String from, to;

    /**
     *
     * @param source {@link com.nexusnode.launcher.game.GameRepository}
     * @param from the version id.
     */
    public RenameVersionEvent(Object source, String from, String to) {
        super(source);
        this.from = from;
        this.to = to;
    }

    public String getFromVersion() {
        return from;
    }

    public String getToVersion() {
        return to;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("from", from)
                .append("to", to)
                .toString();
    }
}
