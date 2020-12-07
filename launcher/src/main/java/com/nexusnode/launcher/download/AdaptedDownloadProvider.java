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
package com.nexusnode.launcher.download;

import com.nexusnode.launcher.util.io.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The download provider that changes the real download source in need.
 *
 * @author bluebird6900
 */
public class AdaptedDownloadProvider implements DownloadProvider {

    private List<DownloadProvider> downloadProviderCandidates;

    public void setDownloadProviderCandidates(List<DownloadProvider> downloadProviderCandidates) {
        this.downloadProviderCandidates = new ArrayList<>(downloadProviderCandidates);
    }

    public DownloadProvider getPreferredDownloadProvider() {
        List<DownloadProvider> d = downloadProviderCandidates;
        if (d == null || d.isEmpty()) {
            throw new IllegalStateException("No download provider candidate");
        }
        return d.get(0);
    }

    @Override
    public String getVersionListURL() {
        return getPreferredDownloadProvider().getVersionListURL();
    }

    @Override
    public String getAssetBaseURL() {
        return getPreferredDownloadProvider().getAssetBaseURL();
    }

    @Override
    public String injectURL(String baseURL) {
        return getPreferredDownloadProvider().injectURL(baseURL);
    }

    @Override
    public List<URL> injectURLWithCandidates(String baseURL) {
        List<DownloadProvider> d = downloadProviderCandidates;
        List<URL> results = new ArrayList<>(d.size());
        for (DownloadProvider downloadProvider : d) {
            results.add(NetworkUtils.toURL(downloadProvider.injectURL(baseURL)));
        }
        return results;
    }

    @Override
    public List<URL> injectURLsWithCandidates(List<String> urls) {
        List<DownloadProvider> d = downloadProviderCandidates;
        List<URL> results = new ArrayList<>(d.size());
        for (DownloadProvider downloadProvider : d) {
            for (String baseURL : urls) {
                results.add(NetworkUtils.toURL(downloadProvider.injectURL(baseURL)));
            }
        }
        return results;
    }

    @Override
    public VersionList<?> getVersionListById(String id) {
        return getPreferredDownloadProvider().getVersionListById(id);
    }

    @Override
    public int getConcurrency() {
        return getPreferredDownloadProvider().getConcurrency();
    }
}
