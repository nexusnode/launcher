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
package com.nexusnode.launcher.setting;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import com.nexusnode.launcher.download.DefaultDependencyManager;
import com.nexusnode.launcher.download.DownloadProvider;
import com.nexusnode.launcher.event.EventBus;
import com.nexusnode.launcher.event.EventPriority;
import com.nexusnode.launcher.event.RefreshedVersionsEvent;
import com.nexusnode.launcher.game.HMCLCacheRepository;
import com.nexusnode.launcher.game.HMCLGameRepository;
import com.nexusnode.launcher.game.Version;
import com.nexusnode.launcher.ui.WeakListenerHolder;
import com.nexusnode.launcher.util.ToStringBuilder;
import com.nexusnode.launcher.util.javafx.ObservableHelper;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Optional;

import static com.nexusnode.launcher.ui.FXUtils.onInvalidating;
import static com.nexusnode.launcher.ui.FXUtils.runInFX;

/**
 *
 * @author bluebird6900
 */
@JsonAdapter(Profile.Serializer.class)
public final class Profile implements Observable {
    private final WeakListenerHolder listenerHolder = new WeakListenerHolder();
    private final HMCLGameRepository repository;

    private final StringProperty selectedVersion = new SimpleStringProperty();

    public StringProperty selectedVersionProperty() {
        return selectedVersion;
    }

    public String getSelectedVersion() {
        return selectedVersion.get();
    }

    public void setSelectedVersion(String selectedVersion) {
        this.selectedVersion.set(selectedVersion);
    }

    private final ObjectProperty<File> gameDir;

    public ObjectProperty<File> gameDirProperty() {
        return gameDir;
    }

    public File getGameDir() {
        return gameDir.get();
    }

    public void setGameDir(File gameDir) {
        this.gameDir.set(gameDir);
    }

    private final ReadOnlyObjectWrapper<VersionSetting> global = new ReadOnlyObjectWrapper<>(this, "global");

    public ReadOnlyObjectProperty<VersionSetting> globalProperty() {
        return global.getReadOnlyProperty();
    }

    public VersionSetting getGlobal() {
        return global.get();
    }

    private final SimpleStringProperty name;

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    private BooleanProperty useRelativePath = new SimpleBooleanProperty(this, "useRelativePath", false);

    public BooleanProperty useRelativePathProperty() {
        return useRelativePath;
    }

    public boolean isUseRelativePath() {
        return useRelativePath.get();
    }

    public void setUseRelativePath(boolean useRelativePath) {
        this.useRelativePath.set(useRelativePath);
    }

    public Profile(String name) {
        this(name, new File(".minecraft"));
    }

    public Profile(String name, File initialGameDir) {
        this(name, initialGameDir, new VersionSetting());
    }

    public Profile(String name, File initialGameDir, VersionSetting global) {
        this(name, initialGameDir, global, null, false);
    }

    public Profile(String name, File initialGameDir, VersionSetting global, String selectedVersion, boolean useRelativePath) {
        this.name = new SimpleStringProperty(this, "name", name);
        gameDir = new SimpleObjectProperty<>(this, "gameDir", initialGameDir);
        repository = new HMCLGameRepository(this, initialGameDir);
        this.global.set(global == null ? new VersionSetting() : global);
        this.selectedVersion.set(selectedVersion);
        this.useRelativePath.set(useRelativePath);

        gameDir.addListener((a, b, newValue) -> repository.changeDirectory(newValue));
        this.selectedVersion.addListener(o -> checkSelectedVersion());
        listenerHolder.add(EventBus.EVENT_BUS.channel(RefreshedVersionsEvent.class).registerWeak(event -> checkSelectedVersion(), EventPriority.HIGHEST));

        addPropertyChangedListener(onInvalidating(this::invalidate));
    }

    private void checkSelectedVersion() {
        runInFX(() -> {
            if (!repository.isLoaded()) return;
            String newValue = selectedVersion.get();
            if (!repository.hasVersion(newValue)) {
                Optional<String> version = repository.getVersions().stream().findFirst().map(Version::getId);
                if (version.isPresent())
                    selectedVersion.setValue(version.get());
                else if (newValue != null)
                    selectedVersion.setValue(null);
            }
        });
    }

    public HMCLGameRepository getRepository() {
        return repository;
    }

    public DefaultDependencyManager getDependency() {
        return getDependency(DownloadProviders.getDownloadProvider());
    }

    public DefaultDependencyManager getDependency(DownloadProvider downloadProvider) {
        return new DefaultDependencyManager(repository, downloadProvider, HMCLCacheRepository.REPOSITORY);
    }

    public VersionSetting getVersionSetting(String id) {
        return repository.getVersionSetting(id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("gameDir", getGameDir())
                .append("name", getName())
                .append("useRelativePath", isUseRelativePath())
                .toString();
    }

    private void addPropertyChangedListener(InvalidationListener listener) {
        name.addListener(listener);
        global.addListener(listener);
        gameDir.addListener(listener);
        useRelativePath.addListener(listener);
        global.get().addPropertyChangedListener(listener);
        selectedVersion.addListener(listener);
    }

    private ObservableHelper observableHelper = new ObservableHelper(this);

    @Override
    public void addListener(InvalidationListener listener) {
        observableHelper.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observableHelper.removeListener(listener);
    }

    private void invalidate() {
        Platform.runLater(observableHelper::invalidate);
    }

    public static final class Serializer implements JsonSerializer<Profile>, JsonDeserializer<Profile> {
        @Override
        public JsonElement serialize(Profile src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null)
                return JsonNull.INSTANCE;

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("global", context.serialize(src.getGlobal()));
            jsonObject.addProperty("gameDir", src.getGameDir().getPath());
            jsonObject.addProperty("useRelativePath", src.isUseRelativePath());
            jsonObject.addProperty("selectedMinecraftVersion", src.getSelectedVersion());

            return jsonObject;
        }

        @Override
        public Profile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == JsonNull.INSTANCE || !(json instanceof JsonObject)) return null;
            JsonObject obj = (JsonObject) json;
            String gameDir = Optional.ofNullable(obj.get("gameDir")).map(JsonElement::getAsString).orElse("");

            return new Profile("Default",
                    new File(gameDir),
                    context.deserialize(obj.get("global"), VersionSetting.class),
                    Optional.ofNullable(obj.get("selectedMinecraftVersion")).map(JsonElement::getAsString).orElse(""),
                    Optional.ofNullable(obj.get("useRelativePath")).map(JsonElement::getAsBoolean).orElse(false));
        }

    }
}
