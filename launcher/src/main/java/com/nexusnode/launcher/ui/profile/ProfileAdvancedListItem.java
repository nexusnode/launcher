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
package com.nexusnode.launcher.ui.profile;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import com.nexusnode.launcher.setting.Profile;
import com.nexusnode.launcher.setting.Profiles;
import com.nexusnode.launcher.setting.Theme;
import com.nexusnode.launcher.ui.SVG;
import com.nexusnode.launcher.ui.construct.AdvancedListItem;

import static com.nexusnode.launcher.ui.FXUtils.newImage;

public class ProfileAdvancedListItem extends AdvancedListItem {
    private ObjectProperty<Profile> profile = new SimpleObjectProperty<Profile>() {

        @Override
        protected void invalidated() {
            Profile profile = get();
            if (profile == null) {
            } else {
                setTitle(Profiles.getProfileDisplayName(profile));
                setSubtitle(profile.getGameDir().toString());
            }
        }
    };

    public ProfileAdvancedListItem() {
        setImage(newImage("/assets/img/craft_table.png"));
        setRightGraphic(SVG.viewList(Theme.blackFillBinding(), -1, -1));
    }

    public ObjectProperty<Profile> profileProperty() {
        return profile;
    }
}
