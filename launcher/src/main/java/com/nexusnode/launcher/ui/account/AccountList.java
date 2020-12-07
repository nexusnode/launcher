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

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import com.nexusnode.launcher.auth.Account;
import com.nexusnode.launcher.ui.Controllers;
import com.nexusnode.launcher.ui.ListPage;
import com.nexusnode.launcher.ui.decorator.DecoratorPage;
import com.nexusnode.launcher.util.javafx.MappedObservableList;

import static com.nexusnode.launcher.util.i18n.I18n.i18n;
import static com.nexusnode.launcher.util.javafx.ExtendedProperties.createSelectedItemPropertyFor;

public class AccountList extends ListPage<AccountListItem> implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(i18n("account.manage")));
    private final ListProperty<Account> accounts = new SimpleListProperty<>(this, "accounts", FXCollections.observableArrayList());
    private final ObjectProperty<Account> selectedAccount;

    public AccountList() {
        setItems(MappedObservableList.create(accounts, AccountListItem::new));
        selectedAccount = createSelectedItemPropertyFor(getItems(), Account.class);
    }

    public ObjectProperty<Account> selectedAccountProperty() {
        return selectedAccount;
    }

    public ListProperty<Account> accountsProperty() {
        return accounts;
    }

    @Override
    public void add() {
        Controllers.dialog(new AddAccountPane());
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }
}
