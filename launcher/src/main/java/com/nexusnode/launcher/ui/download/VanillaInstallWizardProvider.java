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

import javafx.scene.Node;
import com.nexusnode.launcher.download.DefaultDependencyManager;
import com.nexusnode.launcher.download.GameBuilder;
import com.nexusnode.launcher.download.RemoteVersion;
import com.nexusnode.launcher.setting.DownloadProviders;
import com.nexusnode.launcher.setting.Profile;
import com.nexusnode.launcher.task.Schedulers;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.ui.wizard.WizardController;
import com.nexusnode.launcher.ui.wizard.WizardProvider;

import java.util.Map;

import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public final class VanillaInstallWizardProvider implements WizardProvider {
    private final Profile profile;
    private final DefaultDependencyManager dependencyManager;
    private final InstallerWizardDownloadProvider downloadProvider;

    public VanillaInstallWizardProvider(Profile profile) {
        this.profile = profile;
        this.downloadProvider = new InstallerWizardDownloadProvider(DownloadProviders.getDownloadProvider());
        this.dependencyManager = profile.getDependency(downloadProvider);
    }

    @Override
    public void start(Map<String, Object> settings) {
        settings.put(PROFILE, profile);
    }

    private Task<Void> finishVersionDownloadingAsync(Map<String, Object> settings) {
        GameBuilder builder = dependencyManager.gameBuilder();

        String name = (String) settings.get("name");
        builder.name(name);
        builder.gameVersion(((RemoteVersion) settings.get("game")).getGameVersion());

        for (Map.Entry<String, Object> entry : settings.entrySet())
            if (!"game".equals(entry.getKey()) && entry.getValue() instanceof RemoteVersion)
                builder.version((RemoteVersion) entry.getValue());

        return builder.buildAsync().whenComplete(any -> profile.getRepository().refreshVersions())
                .thenRunAsync(Schedulers.javafx(), () -> profile.setSelectedVersion(name));
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("title", i18n("install.new_game"));
        settings.put("success_message", i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> UpdateInstallerWizardProvider.alertFailureMessage(exception, next));

        return finishVersionDownloadingAsync(settings);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer.game")), "", downloadProvider, "game",
                        () -> controller.onNext(new InstallersPage(controller, profile.getRepository(), ((RemoteVersion) controller.getSettings().get("game")).getGameVersion(), downloadProvider)));
            default:
                throw new IllegalStateException("error step " + step + ", settings: " + settings + ", pages: " + controller.getPages());
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }

    public static final String PROFILE = "PROFILE";
}