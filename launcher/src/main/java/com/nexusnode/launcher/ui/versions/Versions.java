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

import javafx.stage.FileChooser;
import com.nexusnode.launcher.download.game.GameAssetDownloadTask;
import com.nexusnode.launcher.game.GameDirectoryType;
import com.nexusnode.launcher.game.GameRepository;
import com.nexusnode.launcher.game.LauncherHelper;
import com.nexusnode.launcher.setting.Accounts;
import com.nexusnode.launcher.setting.Profile;
import com.nexusnode.launcher.task.Schedulers;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.task.TaskExecutor;
import com.nexusnode.launcher.ui.Controllers;
import com.nexusnode.launcher.ui.FXUtils;
import com.nexusnode.launcher.ui.construct.MessageDialogPane;
import com.nexusnode.launcher.ui.construct.PromptDialogPane;
import com.nexusnode.launcher.ui.construct.Validator;
import com.nexusnode.launcher.util.Logging;
import com.nexusnode.launcher.util.StringUtils;
import com.nexusnode.launcher.util.io.FileUtils;
import com.nexusnode.launcher.util.platform.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public class Versions {

    public static void deleteVersion(Profile profile, String version) {
        boolean isIndependent = profile.getVersionSetting(version).getGameDirType() == GameDirectoryType.VERSION_FOLDER;
        boolean isMovingToTrashSupported = FileUtils.isMovingToTrashSupported();
        String message = isIndependent ? i18n("version.manage.remove.confirm.independent", version) :
                isMovingToTrashSupported ? i18n("version.manage.remove.confirm.trash", version, version + "_removed") :
                        i18n("version.manage.remove.confirm", version);
        Controllers.confirm(message, i18n("message.confirm"), () -> {
            profile.getRepository().removeVersionFromDisk(version);
        }, null);
    }

    public static CompletableFuture<String> renameVersion(Profile profile, String version) {
        return Controllers.prompt(i18n("version.manage.rename.message"), (res, resolve, reject) -> {
            if (profile.getRepository().renameVersion(version, res)) {
                profile.getRepository().refreshVersionsAsync().start();
                resolve.run();
            } else {
                reject.accept(i18n("version.manage.rename.fail"));
            }
        }, version);
    }

    public static void exportVersion(Profile profile, String version) {
        //TODO
    }

    public static void openFolder(Profile profile, String version) {
        FXUtils.openFolder(profile.getRepository().getRunDirectory(version));
    }

    public static void duplicateVersion(Profile profile, String version) {
        Controllers.prompt(
                new PromptDialogPane.Builder(i18n("version.manage.duplicate.prompt"), (res, resolve, reject) -> {
                    String newVersionName = ((PromptDialogPane.Builder.StringQuestion) res.get(0)).getValue();
                    boolean copySaves = ((PromptDialogPane.Builder.BooleanQuestion) res.get(1)).getValue();
                    Task.runAsync(() -> profile.getRepository().duplicateVersion(version, newVersionName, copySaves))
                            .thenComposeAsync(profile.getRepository().refreshVersionsAsync())
                            .whenComplete(Schedulers.javafx(), (result, exception) -> {
                                if (exception == null) {
                                    resolve.run();
                                } else {
                                    reject.accept(StringUtils.getStackTrace(exception));
                                    profile.getRepository().removeVersionFromDisk(newVersionName);
                                }
                            }).start();
                })
                        .addQuestion(new PromptDialogPane.Builder.StringQuestion(i18n("version.manage.duplicate.confirm"), version,
                                new Validator(i18n("install.new_game.already_exists"), newVersionName -> !profile.getRepository().hasVersion(newVersionName))))
                        .addQuestion(new PromptDialogPane.Builder.BooleanQuestion(i18n("version.manage.duplicate.duplicate_save"), false)));
    }

    public static void updateVersion(Profile profile, String version) {

    }

    public static void updateGameAssets(Profile profile, String version) {
        TaskExecutor executor = new GameAssetDownloadTask(profile.getDependency(), profile.getRepository().getVersion(version), GameAssetDownloadTask.DOWNLOAD_INDEX_FORCIBLY, true)
                .executor();
        Controllers.taskDialog(executor, i18n("version.manage.redownload_assets_index"));
        executor.start();
    }

    public static void cleanVersion(Profile profile, String id) {
        try {
            profile.getRepository().clean(id);
        } catch (IOException e) {
            Logging.LOG.log(Level.WARNING, "Unable to clean game directory", e);
        }
    }

    public static void generateLaunchScript(Profile profile, String id) {
        if (checkForLaunching(profile, id)) {
            GameRepository repository = profile.getRepository();
            FileChooser chooser = new FileChooser();
            if (repository.getRunDirectory(id).isDirectory())
                chooser.setInitialDirectory(repository.getRunDirectory(id));
            chooser.setTitle(i18n("version.launch_script.save"));
            chooser.getExtensionFilters().add(OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS
                    ? new FileChooser.ExtensionFilter(i18n("extension.bat"), "*.bat")
                    : new FileChooser.ExtensionFilter(i18n("extension.sh"), "*.sh"));
            File file = chooser.showSaveDialog(Controllers.getStage());
            if (file != null)
                new LauncherHelper(profile, Accounts.getSelectedAccount(), id).makeLaunchScript(file);
        }
    }

    public static void launch(Profile profile, String id) {
        if (checkForLaunching(profile, id))
            new LauncherHelper(profile, Accounts.getSelectedAccount(), id).launch();
    }

    public static void testGame(Profile profile, String id) {
        if (checkForLaunching(profile, id)) {
            LauncherHelper helper = new LauncherHelper(profile, Accounts.getSelectedAccount(), id);
            helper.setTestMode();
            helper.launch();
        }
    }

    private static boolean checkForLaunching(Profile profile, String id) {
        if (Accounts.getSelectedAccount() == null)
            Controllers.getRootPage().checkAccount();
        else if (id == null || !profile.getRepository().isLoaded() || !profile.getRepository().hasVersion(id))
            Controllers.dialog(i18n("version.empty.launch"), i18n("launch.failed"), MessageDialogPane.MessageType.ERROR, () -> {
                 Controllers.getRootPage().getSelectionModel().select(Controllers.getRootPage().getGameTab());
            });
        else
            return true;
        return false;
    }

    public static void modifyGlobalSettings(Profile profile) {
        VersionSettingsPage page = new VersionSettingsPage();
        page.loadVersion(profile, null);
        Controllers.navigate(page);
    }

    public static void modifyGameSettings(Profile profile, String version) {
        Controllers.getVersionPage().setVersion(version, profile);
        // VersionPage.loadVersion will be invoked after navigation
        Controllers.navigate(Controllers.getVersionPage());
    }
}
