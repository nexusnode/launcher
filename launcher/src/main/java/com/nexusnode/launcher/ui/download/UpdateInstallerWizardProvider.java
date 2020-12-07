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
import com.nexusnode.launcher.download.ArtifactMalformedException;
import com.nexusnode.launcher.download.DefaultDependencyManager;
import com.nexusnode.launcher.download.RemoteVersion;
import com.nexusnode.launcher.download.VersionMismatchException;
import com.nexusnode.launcher.download.game.GameAssetIndexDownloadTask;
import com.nexusnode.launcher.download.game.LibraryDownloadException;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.setting.DownloadProviders;
import com.nexusnode.launcher.setting.Profile;
import com.nexusnode.launcher.task.DownloadException;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.ui.Controllers;
import com.nexusnode.launcher.ui.construct.MessageDialogPane;
import com.nexusnode.launcher.ui.wizard.WizardController;
import com.nexusnode.launcher.ui.wizard.WizardProvider;
import com.nexusnode.launcher.util.StringUtils;
import com.nexusnode.launcher.util.i18n.I18n;
import com.nexusnode.launcher.util.io.ResponseCodeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public final class UpdateInstallerWizardProvider implements WizardProvider {
    private final Profile profile;
    private final DefaultDependencyManager dependencyManager;
    private final String gameVersion;
    private final Version version;
    private final String libraryId;
    private final String oldLibraryVersion;
    private final InstallerWizardDownloadProvider downloadProvider;

    public UpdateInstallerWizardProvider(@NotNull Profile profile, @NotNull String gameVersion, @NotNull Version version, @NotNull String libraryId, @Nullable String oldLibraryVersion) {
        this.profile = profile;
        this.gameVersion = gameVersion;
        this.version = version;
        this.libraryId = libraryId;
        this.oldLibraryVersion = oldLibraryVersion;
        this.downloadProvider = new InstallerWizardDownloadProvider(DownloadProviders.getDownloadProvider());
        this.dependencyManager = profile.getDependency(downloadProvider);
    }

    @Override
    public void start(Map<String, Object> settings) {
    }

    @Override
    public Object finish(Map<String, Object> settings) {
        settings.put("title", i18n("install.change_version"));
        settings.put("success_message", i18n("install.success"));
        settings.put("failure_callback", (FailureCallback) (settings1, exception, next) -> alertFailureMessage(exception, next));

        // We remove library but not save it,
        // so if installation failed will not break down current version.
        Task<Version> ret = Task.supplyAsync(() -> version);
        List<String> stages = new ArrayList<>();
        for (Object value : settings.values()) {
            if (value instanceof RemoteVersion) {
                RemoteVersion remoteVersion = (RemoteVersion) value;
                ret = ret.thenComposeAsync(version -> dependencyManager.installLibraryAsync(version, remoteVersion));
                stages.add(String.format("hmcl.install.%s:%s", remoteVersion.getLibraryId(), remoteVersion.getSelfVersion()));
                if ("game".equals(remoteVersion.getLibraryId())) {
                    stages.add("hmcl.install.assets");
                }
            } else if (value instanceof RemoveVersionAction) {
                ret = ret.thenComposeAsync(version -> dependencyManager.removeLibraryAsync(version, ((RemoveVersionAction) value).libraryId));
            }
        }

        return ret.thenComposeAsync(profile.getRepository()::save).thenComposeAsync(profile.getRepository().refreshVersionsAsync()).withStagesHint(stages);
    }

    @Override
    public Node createPage(WizardController controller, int step, Map<String, Object> settings) {
        switch (step) {
            case 0:
                return new VersionsPage(controller, i18n("install.installer.choose", i18n("install.installer." + libraryId)), gameVersion, downloadProvider, libraryId, () -> {
                    if (oldLibraryVersion == null) {
                        controller.onFinish();
                    } else if ("game".equals(libraryId)) {
                        String newGameVersion = ((RemoteVersion) settings.get(libraryId)).getSelfVersion();
                        controller.onNext(new AdditionalInstallersPage(newGameVersion, version, controller, profile.getRepository(), downloadProvider));
                    } else {
                        Controllers.confirm(i18n("install.change_version.confirm", i18n("install.installer." + libraryId), oldLibraryVersion, ((RemoteVersion) settings.get(libraryId)).getSelfVersion()),
                                i18n("install.change_version"), controller::onFinish, controller::onCancel);
                    }
                });
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public boolean cancelIfCannotGoBack() {
        // VersionsPage will call wizardController.onPrev(cleanUp = true) when list is empty.
        // So we cancel this wizard when VersionPage calls the method.
        return true;
    }

    public static void alertFailureMessage(Exception exception, Runnable next) {
        if (exception instanceof LibraryDownloadException) {
            String message = i18n("launch.failed.download_library", ((LibraryDownloadException) exception).getLibrary().getName()) + "\n";
            if (exception.getCause() instanceof ResponseCodeException) {
                ResponseCodeException rce = (ResponseCodeException) exception.getCause();
                int responseCode = rce.getResponseCode();
                URL url = rce.getUrl();
                if (responseCode == 404)
                    message += i18n("download.code.404", url);
                else
                    message += i18n("download.failed", url, responseCode);
            } else {
                message += StringUtils.getStackTrace(exception.getCause());
            }
            Controllers.dialog(message, i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof DownloadException) {
            if (exception.getCause() instanceof SocketTimeoutException) {
                Controllers.dialog(i18n("install.failed.downloading.timeout", ((DownloadException) exception).getUrl()), i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
            } else if (exception.getCause() instanceof ResponseCodeException) {
                ResponseCodeException responseCodeException = (ResponseCodeException) exception.getCause();
                if (I18n.hasKey("download.code." + responseCodeException.getResponseCode())) {
                    Controllers.dialog(i18n("download.code." + responseCodeException.getResponseCode(), ((DownloadException) exception).getUrl()), i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
                } else {
                    Controllers.dialog(i18n("install.failed.downloading.detail", ((DownloadException) exception).getUrl()) + "\n" + StringUtils.getStackTrace(exception.getCause()), i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
                }
            } else {
                Controllers.dialog(i18n("install.failed.downloading.detail", ((DownloadException) exception).getUrl()) + "\n" + StringUtils.getStackTrace(exception.getCause()), i18n("install.failed.downloading"), MessageDialogPane.MessageType.ERROR, next);
            }
        } else if (exception instanceof DefaultDependencyManager.UnsupportedLibraryInstallerException) {
            Controllers.dialog(i18n("install.failed.install_online"), i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof ArtifactMalformedException || exception instanceof ZipException) {
            Controllers.dialog(i18n("install.failed.malformed"), i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof GameAssetIndexDownloadTask.GameAssetIndexMalformedException) {
            Controllers.dialog(i18n("assets.index.malformed"), i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else if (exception instanceof VersionMismatchException) {
            VersionMismatchException e = ((VersionMismatchException) exception);
            Controllers.dialog(i18n("install.failed.version_mismatch", e.getExpect(), e.getActual()), i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        } else {
            Controllers.dialog(StringUtils.getStackTrace(exception), i18n("install.failed"), MessageDialogPane.MessageType.ERROR, next);
        }
    }

    public static class RemoveVersionAction {
        private final String libraryId;

        public RemoveVersionAction(String libraryId) {
            this.libraryId = libraryId;
        }
    }
}
