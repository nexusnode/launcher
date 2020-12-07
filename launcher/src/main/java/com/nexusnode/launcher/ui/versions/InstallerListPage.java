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

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.stage.FileChooser;
import com.nexusnode.launcher.download.LibraryAnalyzer;
import com.nexusnode.launcher.game.GameVersion;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.setting.Profile;
import com.nexusnode.launcher.task.Schedulers;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.task.TaskExecutor;
import com.nexusnode.launcher.task.TaskListener;
import com.nexusnode.launcher.ui.*;
import com.nexusnode.launcher.ui.download.UpdateInstallerWizardProvider;
import com.nexusnode.launcher.util.Lang;
import com.nexusnode.launcher.util.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.nexusnode.launcher.ui.FXUtils.runInFX;
import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public class InstallerListPage extends ListPageBase<InstallerItem> {
    private Profile profile;
    private String versionId;
    private Version version;
    private String gameVersion;

    {
        FXUtils.applyDragListener(this, it -> Arrays.asList("jar", "exe").contains(FileUtils.getExtension(it)), mods -> {
            if (!mods.isEmpty())
                doInstallOffline(mods.get(0));
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new InstallerListPageSkin();
    }

    public CompletableFuture<?> loadVersion(Profile profile, String versionId) {
        this.profile = profile;
        this.versionId = versionId;
        this.version = profile.getRepository().getVersion(versionId);
        this.gameVersion = null;

        return CompletableFuture.supplyAsync(() -> {
            gameVersion = GameVersion.minecraftVersion(profile.getRepository().getVersionJar(version)).orElse(null);

            return LibraryAnalyzer.analyze(profile.getRepository().getResolvedPreservingPatchesVersion(versionId));
        }).thenAcceptAsync(analyzer -> {
            Function<String, Consumer<InstallerItem>> removeAction = libraryId -> x -> {
                profile.getDependency().removeLibraryAsync(version, libraryId)
                        .thenComposeAsync(profile.getRepository()::save)
                        .withComposeAsync(profile.getRepository().refreshVersionsAsync())
                        .withRunAsync(Schedulers.javafx(), () -> loadVersion(this.profile, this.versionId))
                        .start();
            };

            itemsProperty().clear();

            for (LibraryAnalyzer.LibraryType type : LibraryAnalyzer.LibraryType.values()) {
                String libraryId = type.getPatchId();
                String libraryVersion = analyzer.getVersion(type).orElse(null);
                Consumer<InstallerItem> action = "game".equals(libraryId) || libraryVersion == null ? null : removeAction.apply(libraryId);
                itemsProperty().add(new InstallerItem(libraryId, libraryVersion, () -> {
                    Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, libraryId, libraryVersion));
                }, action));
            }

            for (LibraryAnalyzer.LibraryMark mark : analyzer) {
                String libraryId = mark.getLibraryId();
                String libraryVersion = mark.getLibraryVersion();

                // we have done this library above.
                if (LibraryAnalyzer.LibraryType.fromPatchId(libraryId) != null)
                    continue;

                Consumer<InstallerItem> action = removeAction.apply(libraryId);
                if (libraryVersion != null && Lang.test(() -> profile.getDependency().getVersionList(libraryId)))
                    itemsProperty().add(
                            new InstallerItem(libraryId, libraryVersion, () -> {
                                Controllers.getDecorator().startWizard(new UpdateInstallerWizardProvider(profile, gameVersion, version, libraryId, libraryVersion));
                            }, action));
                else
                    itemsProperty().add(new InstallerItem(libraryId, libraryVersion, null, action));
            }
        }, Platform::runLater);
    }

    public void installOffline() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n("install.installer.install_offline.extension"), "*.jar", "*.exe"));
        File file = chooser.showOpenDialog(Controllers.getStage());
        if (file != null) doInstallOffline(file);
    }

    private void doInstallOffline(File file) {
        Task<?> task = profile.getDependency().installLibraryAsync(version, file.toPath())
                .thenComposeAsync(profile.getRepository()::save)
                .thenComposeAsync(profile.getRepository().refreshVersionsAsync());
        task.setName(i18n("install.installer.install_offline"));
        TaskExecutor executor = task.executor(new TaskListener() {
            @Override
            public void onStop(boolean success, TaskExecutor executor) {
                runInFX(() -> {
                    if (success) {
                        loadVersion(profile, versionId);
                        Controllers.dialog(i18n("install.success"));
                    } else {
                        if (executor.getException() == null)
                            return;
                        UpdateInstallerWizardProvider.alertFailureMessage(executor.getException(), null);
                    }
                });
            }
        });
        Controllers.taskDialog(executor, i18n("install.installer.install_offline"));
        executor.start();
    }

    private class InstallerListPageSkin extends ToolbarListPageSkin<InstallerListPage> {

        InstallerListPageSkin() {
            super(InstallerListPage.this);
        }

        @Override
        protected List<Node> initializeToolbar(InstallerListPage skinnable) {
            return Collections.singletonList(
                    createToolbarButton(i18n("install.installer.install_offline"), SVG::plus, skinnable::installOffline));
        }
    }
}
