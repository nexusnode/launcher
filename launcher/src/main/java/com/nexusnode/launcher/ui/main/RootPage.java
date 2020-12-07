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
package com.nexusnode.launcher.ui.main;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import com.nexusnode.launcher.event.EventBus;
import com.nexusnode.launcher.event.RefreshedVersionsEvent;
import com.nexusnode.launcher.game.HMCLGameRepository;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.setting.Accounts;
import com.nexusnode.launcher.setting.Profile;
import com.nexusnode.launcher.setting.Profiles;
import com.nexusnode.launcher.task.Schedulers;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.ui.Controllers;
import com.nexusnode.launcher.ui.FXUtils;
import com.nexusnode.launcher.ui.account.AccountAdvancedListItem;
import com.nexusnode.launcher.ui.account.AccountList;
import com.nexusnode.launcher.ui.account.AddAccountPane;
import com.nexusnode.launcher.ui.construct.AdvancedListBox;
import com.nexusnode.launcher.ui.construct.AdvancedListItem;
import com.nexusnode.launcher.ui.construct.TabHeader;
import com.nexusnode.launcher.ui.decorator.DecoratorTabPage;
import com.nexusnode.launcher.ui.profile.ProfileAdvancedListItem;
import com.nexusnode.launcher.ui.profile.ProfileList;
import com.nexusnode.launcher.ui.versions.GameAdvancedListItem;
import com.nexusnode.launcher.ui.versions.GameList;
import com.nexusnode.launcher.ui.versions.Versions;
import com.nexusnode.launcher.upgrade.UpdateChecker;
import com.nexusnode.launcher.util.io.CompressingUtils;
import com.nexusnode.launcher.util.io.FileUtils;
import com.nexusnode.launcher.util.javafx.BindingMapping;
import com.nexusnode.launcher.util.versioning.VersionNumber;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.nexusnode.launcher.ui.FXUtils.newImage;
import static com.nexusnode.launcher.ui.FXUtils.runInFX;
import static com.nexusnode.launcher.util.i18n.I18n.i18n;

public class RootPage extends DecoratorTabPage {
    private MainPage mainPage = null;
    private SettingsPage settingsPage = null;
    private GameList gameListPage = null;
    private AccountList accountListPage = null;
    private ProfileList profileListPage = null;

    private final TabHeader.Tab mainTab = new TabHeader.Tab("main");
    private final TabHeader.Tab settingsTab = new TabHeader.Tab("settings");
    private final TabHeader.Tab gameTab = new TabHeader.Tab("game");
    private final TabHeader.Tab accountTab = new TabHeader.Tab("account");
    private final TabHeader.Tab profileTab = new TabHeader.Tab("profile");

    public RootPage() {
        setLeftPaneWidth(200);

        EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).register(event -> onRefreshedVersions((HMCLGameRepository) event.getSource()));

        Profile profile = Profiles.getSelectedProfile();
        if (profile != null && profile.getRepository().isLoaded())
            onRefreshedVersions(Profiles.selectedProfileProperty().get().getRepository());

        mainTab.setNodeSupplier(this::getMainPage);
        settingsTab.setNodeSupplier(this::getSettingsPage);
        gameTab.setNodeSupplier(this::getGameListPage);
        accountTab.setNodeSupplier(this::getAccountListPage);
        profileTab.setNodeSupplier(this::getProfileListPage);
        getTabs().setAll(mainTab, settingsTab, gameTab, accountTab, profileTab);
    }

    @Override
    public boolean back() {
        if (mainTab.isSelected()) return true;
        else {
            getSelectionModel().select(mainTab);
            return false;
        }
    }

    @Override
    protected void onNavigated(Node to) {
        backableProperty().set(!(to instanceof MainPage));
        setTitleBarTransparent(to instanceof MainPage);

        super.onNavigated(to);
    }

    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }

    private MainPage getMainPage() {
        if (mainPage == null) {
            MainPage mainPage = new MainPage();
            FXUtils.applyDragListener(mainPage, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
                File modpack = modpacks.get(0);
            });

            FXUtils.onChangeAndOperate(Profiles.selectedVersionProperty(), mainPage::setCurrentGame);
            mainPage.showUpdateProperty().bind(UpdateChecker.outdatedProperty());
            mainPage.latestVersionProperty().bind(
                    BindingMapping.of(UpdateChecker.latestVersionProperty())
                            .map(version -> version == null ? "" : i18n("update.bubble.title", version.getVersion())));

            Profiles.registerVersionsListener(profile -> {
                HMCLGameRepository repository = profile.getRepository();
                List<Version> children = repository.getVersions().parallelStream()
                        .filter(version -> !version.isHidden())
                        .sorted(Comparator.comparing((Version version) -> version.getReleaseTime() == null ? new Date(0L) : version.getReleaseTime())
                                .thenComparing(a -> VersionNumber.asVersion(a.getId())))
                        .collect(Collectors.toList());
                runInFX(() -> {
                    if (profile == Profiles.getSelectedProfile())
                        mainPage.initVersions(profile, children);
                });
            });
            this.mainPage = mainPage;
        }
        return mainPage;
    }

    private SettingsPage getSettingsPage() {
        if (settingsPage == null)
            settingsPage = new SettingsPage();
        return settingsPage;
    }

    private GameList getGameListPage() {
        if (gameListPage == null) {
            gameListPage = new GameList();
            FXUtils.applyDragListener(gameListPage, it -> "zip".equals(FileUtils.getExtension(it)), modpacks -> {
            });
        }
        return gameListPage;
    }

    private AccountList getAccountListPage() {
        if (accountListPage == null) {
            accountListPage = new AccountList();
            accountListPage.selectedAccountProperty().bindBidirectional(Accounts.selectedAccountProperty());
            accountListPage.accountsProperty().bindContent(Accounts.accountsProperty());
        }
        return accountListPage;
    }

    private ProfileList getProfileListPage() {
        if (profileListPage == null) {
            profileListPage = new ProfileList();
            profileListPage.selectedProfileProperty().bindBidirectional(Profiles.selectedProfileProperty());
            profileListPage.profilesProperty().bindContent(Profiles.profilesProperty());
        }
        return profileListPage;
    }

    public Tab getMainTab() {
        return mainTab;
    }

    public Tab getSettingsTab() {
        return settingsTab;
    }

    public Tab getGameTab() {
        return gameTab;
    }

    public Tab getAccountTab() {
        return accountTab;
    }

    public Tab getProfileTab() {
        return profileTab;
    }

    private static class Skin extends SkinBase<RootPage> {

        protected Skin(RootPage control) {
            super(control);

            // first item in left sidebar
            AccountAdvancedListItem accountListItem = new AccountAdvancedListItem();
            accountListItem.activeProperty().bind(control.accountTab.selectedProperty());
            accountListItem.setOnAction(e -> control.getSelectionModel().select(control.accountTab));
            accountListItem.accountProperty().bind(Accounts.selectedAccountProperty());

            // second item in left sidebar
            GameAdvancedListItem gameListItem = new GameAdvancedListItem();
            gameListItem.actionButtonVisibleProperty().bind(Profiles.selectedVersionProperty().isNotNull());
            gameListItem.setOnAction(e -> {
                Profile profile = Profiles.getSelectedProfile();
                String version = Profiles.getSelectedVersion();
                if (version == null) {
                    control.getSelectionModel().select(control.gameTab);
                } else {
                    Versions.modifyGameSettings(profile, version);
                }
            });

            // third item in left sidebar
            AdvancedListItem gameItem = new AdvancedListItem();
            gameItem.activeProperty().bind(control.gameTab.selectedProperty());
            gameItem.setImage(newImage("/assets/img/bookshelf.png"));
            gameItem.setTitle(i18n("version.manage"));
            gameItem.setOnAction(e -> control.getSelectionModel().select(control.gameTab));

            // forth item in left sidebar
            ProfileAdvancedListItem profileListItem = new ProfileAdvancedListItem();
            profileListItem.activeProperty().bind(control.profileTab.selectedProperty());
            profileListItem.setOnAction(e -> control.getSelectionModel().select(control.profileTab));
            profileListItem.profileProperty().bind(Profiles.selectedProfileProperty());

            // fifth item in left sidebar
            AdvancedListItem launcherSettingsItem = new AdvancedListItem();
            launcherSettingsItem.activeProperty().bind(control.settingsTab.selectedProperty());
            launcherSettingsItem.setImage(newImage("/assets/img/command.png"));
            launcherSettingsItem.setTitle(i18n("settings.launcher"));
            launcherSettingsItem.setOnAction(e -> control.getSelectionModel().select(control.settingsTab));

            // the left sidebar
            AdvancedListBox sideBar = new AdvancedListBox()
                    .startCategory(i18n("account").toUpperCase())
                    .add(accountListItem)
                    .startCategory(i18n("version").toUpperCase())
                    .add(gameListItem)
                    .add(gameItem)
                    .startCategory(i18n("profile.title").toUpperCase())
                    .add(profileListItem)
                    .startCategory(i18n("launcher").toUpperCase())
                    .add(launcherSettingsItem);

            // the root page, with the sidebar in left, navigator in center.
            BorderPane root = new BorderPane();
            sideBar.setPrefWidth(200);
            root.setLeft(sideBar);

            {
                control.transitionPane.getStyleClass().add("jfx-decorator-content-container");
                control.transitionPane.getChildren().setAll(getSkinnable().getMainPage());
                FXUtils.setOverflowHidden(control.transitionPane, 8);
                root.setCenter(control.transitionPane);
            }

            getChildren().setAll(root);
        }

    }

    // ==== Accounts ====
    public void checkAccount() {
        if (Accounts.getAccounts().isEmpty())
            Platform.runLater(this::addNewAccount);
    }

    private void addNewAccount() {
        Controllers.dialog(new AddAccountPane());
    }
    // ====

    private boolean checkedModpack = false;

    private void onRefreshedVersions(HMCLGameRepository repository) {
        runInFX(() -> {
            if (!checkedModpack) {
                checkedModpack = true;

                if (repository.getVersionCount() == 0) {

                }
            }

            checkAccount();
        });
    }
}
