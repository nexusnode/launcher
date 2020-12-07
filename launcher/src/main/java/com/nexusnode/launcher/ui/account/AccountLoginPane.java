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
package com.nexusnode.launcher.ui.account;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import com.nexusnode.launcher.auth.Account;
import com.nexusnode.launcher.auth.AuthInfo;
import com.nexusnode.launcher.auth.NoSelectedCharacterException;
import com.nexusnode.launcher.task.Schedulers;
import com.nexusnode.launcher.task.Task;
import com.nexusnode.launcher.ui.FXUtils;
import com.nexusnode.launcher.ui.construct.DialogCloseEvent;

import java.util.function.Consumer;
import java.util.logging.Level;

import static com.nexusnode.launcher.util.Logging.LOG;

public class AccountLoginPane extends StackPane {
    private final Account oldAccount;
    private final Consumer<AuthInfo> success;
    private final Runnable failed;

    @FXML private Label lblUsername;
    @FXML private JFXPasswordField txtPassword;
    @FXML private Label lblCreationWarning;
    @FXML private JFXProgressBar progressBar;

    public AccountLoginPane(Account oldAccount, Consumer<AuthInfo> success, Runnable failed) {
        this.oldAccount = oldAccount;
        this.success = success;
        this.failed = failed;

        FXUtils.loadFXML(this, "/assets/fxml/account-login.fxml");

        lblUsername.setText(oldAccount.getUsername());
        txtPassword.setOnAction(e -> onAccept());
    }

    @FXML
    private void onAccept() {
        String password = txtPassword.getText();
        progressBar.setVisible(true);
        lblCreationWarning.setText("");
        Task.supplyAsync(() -> oldAccount.logInWithPassword(password))
                .whenComplete(Schedulers.javafx(), authInfo -> {
                    success.accept(authInfo);
                    fireEvent(new DialogCloseEvent());
                    progressBar.setVisible(false);
                }, e -> {
                    LOG.log(Level.INFO, "Failed to login with password: " + oldAccount, e);
                    if (e instanceof NoSelectedCharacterException) {
                        fireEvent(new DialogCloseEvent());
                    } else {
                        lblCreationWarning.setText(AddAccountPane.accountException(e));
                    }
                    progressBar.setVisible(false);
                }).start();
    }

    @FXML
    private void onCancel() {
        failed.run();
        fireEvent(new DialogCloseEvent());
    }
}
