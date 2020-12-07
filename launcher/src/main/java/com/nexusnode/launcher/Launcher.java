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
package com.nexusnode.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.nexusnode.launcher.setting.ConfigHolder;
import com.nexusnode.launcher.task.Schedulers;
import com.nexusnode.launcher.task.AsyncTaskExecutor;
import com.nexusnode.launcher.ui.Controllers;
import com.nexusnode.launcher.upgrade.UpdateChecker;
import com.nexusnode.launcher.util.CrashReporter;
import com.nexusnode.launcher.util.Lang;
import com.nexusnode.launcher.util.StringUtils;
import com.nexusnode.launcher.util.platform.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.nexusnode.launcher.ui.FXUtils.runInFX;
import static com.nexusnode.launcher.util.Logging.LOG;
import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public final class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) {
        Thread.currentThread().setUncaughtExceptionHandler(CRASH_REPORTER);

        try {
            try {
                ConfigHolder.init();
            } catch (IOException e) {
                Main.showErrorAndExit(i18n("fatal.config_loading_failure", Paths.get("").toAbsolutePath().normalize()));
            }

            // runLater to ensure ConfigHolder.init() finished initialization
            Platform.runLater(() -> {
                // When launcher visibility is set to "hide and reopen" without Platform.implicitExit = false,
                // Stage.show() cannot work again because JavaFX Toolkit have already shut down.
                Platform.setImplicitExit(false);
                Controllers.initialize(primaryStage);

                UpdateChecker.init();

                primaryStage.show();
            });
        } catch (Throwable e) {
            CRASH_REPORTER.uncaughtException(Thread.currentThread(), e);
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(CRASH_REPORTER);
        AsyncTaskExecutor.setUncaughtExceptionHandler(new CrashReporter(false));

        try {
            LOG.info("*** " + Metadata.TITLE + " ***");
            LOG.info("Operating System: " + System.getProperty("os.name") + ' ' + OperatingSystem.SYSTEM_VERSION);
            LOG.info("Java Version: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
            LOG.info("Java VM Version: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
            LOG.info("Java Home: " + System.getProperty("java.home"));
            LOG.info("Current Directory: " + Paths.get("").toAbsolutePath());
            LOG.info("HMCL Directory: " + Metadata.HMCL_DIRECTORY);
            LOG.info("Memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB");
            ManagementFactory.getMemoryPoolMXBeans().stream().filter(bean -> bean.getName().equals("Metaspace")).findAny()
                    .ifPresent(bean -> LOG.info("Metaspace: " + bean.getUsage().getUsed() / 1024 / 1024 + "MB"));

            launch(args);
        } catch (Throwable e) { // Fucking JavaFX will suppress the exception and will break our crash reporter.
            CRASH_REPORTER.uncaughtException(Thread.currentThread(), e);
        }
    }

    public static void stopApplication() {
        LOG.info("Stopping application.\n" + StringUtils.getStackTrace(Thread.currentThread().getStackTrace()));

        runInFX(() -> {
            if (Controllers.getStage() == null)
                return;
            Controllers.getStage().close();
            Schedulers.shutdown();
            Controllers.shutdown();
            Platform.exit();
            Lang.executeDelayed(OperatingSystem::forceGC, TimeUnit.SECONDS, 5, true);
        });
    }

    public static void stopWithoutPlatform() {
        LOG.info("Stopping application without JavaFX Toolkit.\n" + StringUtils.getStackTrace(Thread.currentThread().getStackTrace()));

        runInFX(() -> {
            if (Controllers.getStage() == null)
                return;
            Controllers.getStage().close();
            Schedulers.shutdown();
            Controllers.shutdown();
            Lang.executeDelayed(OperatingSystem::forceGC, TimeUnit.SECONDS, 5, true);
        });
    }

    public static final CrashReporter CRASH_REPORTER = new CrashReporter(true);
}
