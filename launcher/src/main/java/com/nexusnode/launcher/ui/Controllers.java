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
package com.nexusnode.launcher.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import com.nexusnode.launcher.Launcher;
import com.nexusnode.launcher.Metadata;
import com.nexusnode.launcher.setting.EnumCommonDirectory;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.task.TaskExecutor;
import com.nexusnode.launcher.ui.account.AuthlibInjectorServersPage;
import com.nexusnode.launcher.ui.animation.ContainerAnimations;
import com.nexusnode.launcher.ui.construct.InputDialogPane;
import com.nexusnode.launcher.ui.construct.MessageDialogPane;
import com.nexusnode.launcher.ui.construct.MessageDialogPane.MessageType;
import com.nexusnode.launcher.ui.construct.PromptDialogPane;
import com.nexusnode.launcher.ui.construct.TaskExecutorDialogPane;
import com.nexusnode.launcher.ui.decorator.DecoratorController;
import com.nexusnode.launcher.ui.main.RootPage;
import com.nexusnode.launcher.ui.versions.VersionPage;
import com.nexusnode.launcher.util.FutureCallback;
import com.nexusnode.launcher.util.Logging;
import com.nexusnode.launcher.util.io.FileUtils;
import com.nexusnode.launcher.util.platform.JavaVersion;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.nexusnode.launcher.setting.ConfigHolder.config;
import static com.nexusnode.launcher.ui.FXUtils.newImage;
import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public final class Controllers {

    private static Scene scene;
    private static Stage stage;
    private static VersionPage versionPage = null;
    private static AuthlibInjectorServersPage serversPage = null;
    private static RootPage rootPage;
    private static DecoratorController decorator;

    public static Scene getScene() {
        return scene;
    }

    public static Stage getStage() {
        return stage;
    }

    // FXThread
    public static VersionPage getVersionPage() {
        if (versionPage == null)
            versionPage = new VersionPage();
        return versionPage;
    }

    // FXThread
    public static RootPage getRootPage() {
        if (rootPage == null)
            rootPage = new RootPage();
        return rootPage;
    }

    // FXThread
    public static AuthlibInjectorServersPage getServersPage() {
        if (serversPage == null)
            serversPage = new AuthlibInjectorServersPage();
        return serversPage;
    }

    // FXThread
    public static DecoratorController getDecorator() {
        return decorator;
    }


    public static void initialize(Stage stage) {
        Logging.LOG.info("Start initializing application");

        Controllers.stage = stage;

        stage.setOnCloseRequest(e -> Launcher.stopApplication());

        decorator = new DecoratorController(stage, getRootPage());

        if (config().getCommonDirType() == EnumCommonDirectory.CUSTOM &&
                !FileUtils.canCreateDirectory(config().getCommonDirectory())) {
            config().setCommonDirType(EnumCommonDirectory.DEFAULT);
            dialog(i18n("launcher.cache_directory.invalid"));
        }

        Task.runAsync(JavaVersion::initialize).start();

        scene = new Scene(decorator.getDecorator(), 802, 482);
        stage.setMinHeight(482);
        stage.setMinWidth(802);
        decorator.getDecorator().prefWidthProperty().bind(scene.widthProperty());
        decorator.getDecorator().prefHeightProperty().bind(scene.heightProperty());
        scene.getStylesheets().setAll(config().getTheme().getStylesheets());

        stage.getIcons().add(newImage("/assets/img/icon.png"));
        stage.setTitle(Metadata.TITLE);
        stage.setScene(scene);
    }

    public static void dialog(Region content) {
        if (decorator != null)
            decorator.showDialog(content);
    }

    public static void dialog(String text) {
        dialog(text, null);
    }

    public static void dialog(String text, String title) {
        dialog(text, title, MessageType.INFORMATION);
    }

    public static void dialog(String text, String title, MessageType type) {
        dialog(text, title, type, null);
    }

    public static void dialog(String text, String title, MessageType type, Runnable onAccept) {
        dialog(new MessageDialogPane(text, title, type, onAccept));
    }

    public static void confirm(String text, String title, Runnable onAccept, Runnable onCancel) {
        dialog(new MessageDialogPane(text, title, onAccept, onCancel));
    }

    public static CompletableFuture<String> prompt(String title, FutureCallback<String> onResult) {
        return prompt(title, onResult, "");
    }

    public static CompletableFuture<String> prompt(String title, FutureCallback<String> onResult, String initialValue) {
        InputDialogPane pane = new InputDialogPane(title, initialValue, onResult);
        dialog(pane);
        return pane.getCompletableFuture();
    }

    public static CompletableFuture<List<PromptDialogPane.Builder.Question<?>>> prompt(PromptDialogPane.Builder builder) {
        PromptDialogPane pane = new PromptDialogPane(builder);
        dialog(pane);
        return pane.getCompletableFuture();
    }

    public static TaskExecutorDialogPane taskDialog(TaskExecutor executor, String title) {
        return taskDialog(executor, title, null);
    }

    public static TaskExecutorDialogPane taskDialog(TaskExecutor executor, String title, Consumer<Region> onCancel) {
        TaskExecutorDialogPane pane = new TaskExecutorDialogPane(onCancel);
        pane.setTitle(title);
        pane.setExecutor(executor);
        dialog(pane);
        return pane;
    }

    public static void navigate(Node node) {
        decorator.getNavigator().navigate(node, ContainerAnimations.FADE.getAnimationProducer());
    }

    public static boolean isStopped() {
        return decorator == null;
    }

    public static void shutdown() {
        rootPage = null;
        versionPage = null;
        serversPage = null;
        decorator = null;
        stage = null;
        scene = null;
    }
}
