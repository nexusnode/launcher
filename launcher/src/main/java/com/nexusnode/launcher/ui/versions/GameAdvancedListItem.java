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
package com.nexusnode.launcher.ui.versions;

import javafx.scene.control.Tooltip;
import com.nexusnode.launcher.setting.Profiles;
import com.nexusnode.launcher.setting.Theme;
import com.nexusnode.launcher.ui.FXUtils;
import com.nexusnode.launcher.ui.SVG;
import com.nexusnode.launcher.ui.construct.AdvancedListItem;

import static com.nexusnode.launcher.ui.FXUtils.newImage;
import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public class GameAdvancedListItem extends AdvancedListItem {
    private final Tooltip tooltip;

    public GameAdvancedListItem() {
        tooltip = new Tooltip();
        FXUtils.installFastTooltip(this, tooltip);

        FXUtils.onChangeAndOperate(Profiles.selectedVersionProperty(), version -> {
            if (version != null && Profiles.getSelectedProfile() != null &&
                    Profiles.getSelectedProfile().getRepository().hasVersion(version)) {
                setTitle(version);
                setSubtitle(null);
                setImage(Profiles.getSelectedProfile().getRepository().getVersionIconImage(version));
                tooltip.setText(version);
            } else {
                setTitle(i18n("version.empty"));
                setSubtitle(i18n("version.empty.add"));
                setImage(newImage("/assets/img/grass.png"));
                tooltip.setText("");
            }
        });

        setRightGraphic(SVG.gear(Theme.blackFillBinding(), -1, -1));
    }
}
