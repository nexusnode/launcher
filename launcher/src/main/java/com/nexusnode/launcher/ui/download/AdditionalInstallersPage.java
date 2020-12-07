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
package com.nexusnode.launcher.ui.download;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import com.nexusnode.launcher.download.LibraryAnalyzer;
import com.nexusnode.launcher.download.RemoteVersion;
import com.nexusnode.launcher.game.GameRepository;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.ui.wizard.WizardController;
import com.nexusnode.launcher.util.Lang;

import java.util.Map;
import java.util.Optional;

import static com.nexusnode.launcher.download.LibraryAnalyzer.LibraryType.*;
import static com.nexusnode.launcher.util.i18n.I18n.i18n;

class AdditionalInstallersPage extends InstallersPage {
    protected final BooleanProperty compatible = new SimpleBooleanProperty();
    protected final GameRepository repository;
    protected final String gameVersion;
    protected final Version version;

    public AdditionalInstallersPage(String gameVersion, Version version, WizardController controller, GameRepository repository, InstallerWizardDownloadProvider downloadProvider) {
        super(controller, repository, gameVersion, downloadProvider);
        this.gameVersion = gameVersion;
        this.version = version;
        this.repository = repository;

        txtName.getValidators().clear();
        txtName.setText(version.getId());
        txtName.setEditable(false);

        installable.bind(Bindings.createBooleanBinding(
                () -> compatible.get() && txtName.validate(),
                txtName.textProperty(), compatible));

        InstallerPageItem[] libraries = new InstallerPageItem[]{game, fabric, forge, liteLoader, optiFine};

        for (InstallerPageItem library : libraries) {
            String libraryId = library.id;
            if (libraryId.equals("game")) continue;
            library.removeAction.set(e -> {
                controller.getSettings().put(libraryId, new UpdateInstallerWizardProvider.RemoveVersionAction(libraryId));
                reload();
            });
        }
    }

    @Override
    protected void onInstall() {
        controller.onFinish();
    }

    @Override
    public String getTitle() {
        return i18n("settings.tabs.installers");
    }

    private String getVersion(String id) {
        return Optional.ofNullable(controller.getSettings().get(id))
                .flatMap(it -> Lang.tryCast(it, RemoteVersion.class))
                .map(RemoteVersion::getSelfVersion).orElse(null);
    }

    @Override
    protected void reload() {
        LibraryAnalyzer analyzer = LibraryAnalyzer.analyze(version.resolvePreservingPatches(repository));
        String game = analyzer.getVersion(MINECRAFT).orElse(null);
        String fabric = analyzer.getVersion(FABRIC).orElse(null);
        String forge = analyzer.getVersion(FORGE).orElse(null);
        String liteLoader = analyzer.getVersion(LITELOADER).orElse(null);
        String optiFine = analyzer.getVersion(OPTIFINE).orElse(null);

        InstallerPageItem[] libraries = new InstallerPageItem[]{this.game, this.fabric, this.forge, this.liteLoader, this.optiFine};
        String[] versions = new String[]{game, fabric, forge, liteLoader, optiFine};

        String currentGameVersion = Lang.nonNull(getVersion("game"), game);

        boolean compatible = true;
        for (int i = 0; i < libraries.length; ++i) {
            String libraryId = libraries[i].id;
            String libraryVersion = Lang.nonNull(getVersion(libraryId), versions[i]);
            boolean alreadyInstalled = versions[i] != null && !(controller.getSettings().get(libraryId) instanceof UpdateInstallerWizardProvider.RemoveVersionAction);
            if (!"game".equals(libraryId) && currentGameVersion != null && !currentGameVersion.equals(game) && getVersion(libraryId) == null && alreadyInstalled) {
                // For third-party libraries, if game version is being changed, and the library is not being reinstalled,
                // warns the user that we should update the library.
                libraries[i].label.set(i18n("install.installer.change_version", i18n("install.installer." + libraryId), libraryVersion));
                libraries[i].removable.set(true);
                compatible = false;
            } else if (alreadyInstalled || getVersion(libraryId) != null) {
                libraries[i].label.set(i18n("install.installer.version", i18n("install.installer." + libraryId), libraryVersion));
                libraries[i].removable.set(true);
            } else {
                libraries[i].label.set(i18n("install.installer.not_installed", i18n("install.installer." + libraryId)));
                libraries[i].removable.set(false);
            }
        }
        this.compatible.set(compatible);
    }

    @Override
    public void cleanup(Map<String, Object> settings) {
    }
}
