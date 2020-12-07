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

import com.nexusnode.launcher.util.platform.ManagedProcess;

/**
 * This event gets fired when minecraft process exited successfully and the exit code is 0.
 * <br>
 * This event is fired on the {@link com.nexusnode.launcher.event.EventBus#EVENT_BUS}
 *
 * @author bluebird6900
 */
public class ProcessStoppedEvent extends Event {

    private final ManagedProcess process;

    /**
     * Constructor.
     *
     * @param source {@link com.nexusnode.launcher.launch.ExitWaiter}
     * @param process minecraft process
     */
    public ProcessStoppedEvent(Object source, ManagedProcess process) {
        super(source);
        this.process = process;
    }

    public ManagedProcess getProcess() {
        return process;
    }
}
