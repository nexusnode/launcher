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

import com.nexusnode.launcher.auth.Account;
import com.nexusnode.launcher.auth.AuthInfo;
import com.nexusnode.launcher.auth.AuthenticationException;
import com.nexusnode.launcher.auth.yggdrasil.YggdrasilAccount;
import com.nexusnode.launcher.ui.account.AccountLoginPane;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.nexusnode.launcher.ui.FXUtils.runInFX;

public final class DialogController {

    public static AuthInfo logIn(Account account) throws CancellationException, AuthenticationException, InterruptedException {
        if (account instanceof YggdrasilAccount) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<AuthInfo> res = new AtomicReference<>(null);
            runInFX(() -> {
                AccountLoginPane pane = new AccountLoginPane(account, it -> {
                        res.set(it);
                        latch.countDown();
                }, latch::countDown);
                Controllers.dialog(pane);
            });
            latch.await();
            return Optional.ofNullable(res.get()).orElseThrow(CancellationException::new);
        }
        return account.logIn();
    }
}
