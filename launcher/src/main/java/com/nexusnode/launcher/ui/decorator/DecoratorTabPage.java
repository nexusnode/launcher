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
package com.nexusnode.launcher.ui.decorator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import com.nexusnode.launcher.ui.animation.ContainerAnimations;
import com.nexusnode.launcher.ui.construct.Navigator;
import com.nexusnode.launcher.ui.construct.TabControl;
import com.nexusnode.launcher.ui.construct.TabHeader;

public abstract class DecoratorTabPage extends DecoratorTransitionPage implements TabControl {

    public DecoratorTabPage() {
        getSelectionModel().selectedItemProperty().addListener((a, b, newValue) -> {
            if (newValue.getNode() == null && newValue.getNodeSupplier() != null) {
                newValue.setNode(newValue.getNodeSupplier().get());
            }
            if (newValue.getNode() != null) {
                onNavigating(getCurrentPage());
                if (getCurrentPage() != null) getCurrentPage().fireEvent(new Navigator.NavigationEvent(null, getCurrentPage(), Navigator.NavigationEvent.NAVIGATING));
                navigate(newValue.getNode(), ContainerAnimations.FADE.getAnimationProducer());
                onNavigated(getCurrentPage());
                if (getCurrentPage() != null) getCurrentPage().fireEvent(new Navigator.NavigationEvent(null, getCurrentPage(), Navigator.NavigationEvent.NAVIGATED));
            }
        });
    }

    public DecoratorTabPage(TabHeader.Tab... tabs) {
        this();
        if (tabs != null) {
            getTabs().addAll(tabs);
        }
    }

    private ObservableList<TabHeader.Tab> tabs = FXCollections.observableArrayList();

    @Override
    public ObservableList<TabHeader.Tab> getTabs() {
        return tabs;
    }

    private final ObjectProperty<SingleSelectionModel<TabHeader.Tab>> selectionModel = new SimpleObjectProperty<>(this, "selectionModel", new TabControl.TabControlSelectionModel(this));

    public SingleSelectionModel<TabHeader.Tab> getSelectionModel() {
        return selectionModel.get();
    }

    public ObjectProperty<SingleSelectionModel<TabHeader.Tab>> selectionModelProperty() {
        return selectionModel;
    }

    public void setSelectionModel(SingleSelectionModel<TabHeader.Tab> selectionModel) {
        this.selectionModel.set(selectionModel);
    }
}
