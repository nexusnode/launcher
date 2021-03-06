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

import com.nexusnode.launcher.util.StringUtils;
import com.nexusnode.launcher.util.platform.CommandBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bluebird6900
 */
public final class VersionLibraryBuilder {
    private final Version version;
    private final List<String> mcArgs;
    private final List<Argument> game;
    private final boolean useMcArgs;

    public VersionLibraryBuilder(Version version) {
        this.version = version;
        this.mcArgs = version.getMinecraftArguments().map(StringUtils::tokenize).map(ArrayList::new).orElse(null);
        this.game = version.getArguments().map(Arguments::getGame).map(ArrayList::new).orElseGet(ArrayList::new);
        this.useMcArgs = mcArgs != null;
    }

    public Version build() {
        Version ret = version;
        if (useMcArgs) {
            // Since $ will be escaped in linux, and our maintain of minecraftArgument will not cause escaping,
            // so we regenerate the minecraftArgument without escaping.
            ret = ret.setMinecraftArguments(new CommandBuilder().addAllWithoutParsing(mcArgs).toString());
        }
        return ret.setArguments(ret.getArguments().map(args -> args.withGame(game)).orElse(new Arguments(game, null)));
    }

    public void removeTweakClass(String target) {
        replaceTweakClass(target, null, false);
    }

    /**
     * Replace existing tweak class without reordering.
     * If the tweak class does not exist, the new tweak class will be appended to the end of argument list.
     * If the tweak class appears more than one time, the tweak classes will be removed excluding the first one.
     *
     * @param target the tweak class to replace
     * @param replacement the new tweak class to be replaced with
     */
    public void replaceTweakClass(String target, String replacement) {
        replaceTweakClass(target, replacement, true);
    }

    /**
     * Replace existing tweak class and add the new tweak class to the end of argument list.
     *
     * @param target the tweak class to replace
     * @param replacement the new tweak class to be replaced with
     */
    public void addTweakClass(String target, String replacement) {
        replaceTweakClass(target, replacement, false);
    }

    /**
     * Replace existing tweak class.
     * If the tweak class does not exist, the new tweak class will be appended to the end of argument list.
     * If the tweak class appears more than one time, the tweak classes will be removed excluding the first one.
     *
     * @param target the tweak class to replace
     * @param replacement the new tweak class to be replaced with, if null, remove the tweak class only
     * @param inPlace if true, replace the tweak class in place, otherwise add the tweak class to the end of the argument list without replacement.
     */
    public void replaceTweakClass(String target, String replacement, boolean inPlace) {
        if (replacement == null && inPlace)
            throw new IllegalArgumentException("Replacement cannot be null in replace mode");

        boolean replaced = false;
        if (useMcArgs) {
            for (int i = 0; i + 1 < mcArgs.size(); ++i) {
                String arg0Str = mcArgs.get(i);
                String arg1Str = mcArgs.get(i + 1);
                if (arg0Str.equals("--tweakClass") && arg1Str.toLowerCase().contains(target)) {
                    if (!replaced && inPlace) {
                        // for the first one, we replace the tweak class only.
                        mcArgs.set(i + 1, replacement);
                        replaced = true;
                    } else {
                        // otherwise, we remove the duplicate tweak classes.
                        mcArgs.remove(i);
                        mcArgs.remove(i);
                        --i;
                    }
                }
            }
        }

        for (int i = 0; i + 1 < game.size(); ++i) {
            Argument arg0 = game.get(i);
            Argument arg1 = game.get(i + 1);
            if (arg0 instanceof StringArgument && arg1 instanceof StringArgument) {
                // We need to preserve the tokens
                String arg0Str = arg0.toString();
                String arg1Str = arg1.toString();
                if (arg0Str.equals("--tweakClass") && arg1Str.toLowerCase().contains(target)) {
                    if (!replaced && inPlace) {
                        // for the first one, we replace the tweak class only.
                        game.set(i + 1, new StringArgument(replacement));
                        replaced = true;
                    } else {
                        // otherwise, we remove the duplicate tweak classes.
                        game.remove(i);
                        game.remove(i);
                        --i;
                    }
                }
            }
        }

        // if the tweak class does not exist, add a new one to the end.
        if (!replaced && replacement != null) {
            game.add(new StringArgument("--tweakClass"));
            game.add(new StringArgument(replacement));
        }
    }

    public void addArgument(String... args) {
        for (String arg : args)
            game.add(new StringArgument(arg));
    }
}
