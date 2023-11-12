/**
 * VorteX - General utility program written in Java.
 * Copyright (C) 2023 BlockyDotJar (aka. Dominic R.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.blocky.app.vx.updater;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import dev.blocky.app.vx.entities.NodeCreator;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kohsuke.github.*;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import static dev.blocky.app.vx.handler.ActionHandler.invalidAction;

public class ApplicationUpdater
{
    public String getCurrentVersion()
    {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_Version");
    }

    private boolean isOlderVersion(String currentVersion, String latestVersion)
    {
        Comparator<String> versionComparator = Comparator
                .comparing(s -> s, (cv, lv) ->
                {
                    String[] cvArr = cv.split("\\.");
                    String[] lvArr = lv.split("\\.");

                    for (int i = 0; i < Math.min(cvArr.length, lvArr.length); i++)
                    {
                        int cmp = Integer.compare(Integer.parseInt(cvArr[i]), Integer.parseInt(lvArr[i]));

                        if (cmp != 0)
                        {
                            return cmp;
                        }
                    }
                    return Integer.compare(cvArr.length, lvArr.length);
                });

        return versionComparator.compare(currentVersion, latestVersion) >= 0;
    }

    public List<String> newVersion(TextArea detailArea)
    {
        String version = null;
        String releaseLink = null;
        String downloadLink = null;

        try
        {
            GitHub gitHub = new GitHubBuilder().build();
            GHRepository repository = gitHub.getRepository("BlockyDotJar/VorteX");
            GHRelease release = repository.getLatestRelease();

            if (release == null)
            {
                return List.of();
            }

            String currentVersionTag = getCurrentVersion();
            String ghVersionTagName = release.getTagName();

            if (ghVersionTagName == null)
            {
                return List.of();
            }

            String ghVersionTag = ghVersionTagName.substring(1);

            if (!ghVersionTag.matches("[0-9.]+") || isOlderVersion(currentVersionTag, ghVersionTag))
            {
                return List.of();
            }

            if (release.isPrerelease())
            {
                List<GHRelease> releases = repository.listReleases().toList();

                for (GHRelease ghRelease : releases)
                {
                    if (ghRelease.isPrerelease())
                    {
                        continue;
                    }

                    String ghrVersionTag = ghRelease.getTagName().substring(1);

                    if (isOlderVersion(currentVersionTag, ghrVersionTag))
                    {
                        continue;
                    }

                    List<GHAsset> assets = ghRelease.listAssets().toList();

                    if (assets.isEmpty())
                    {
                        return List.of();
                    }

                    version = ghrVersionTag;
                    releaseLink = String.valueOf(ghRelease.getHtmlUrl());
                    downloadLink = assets.get(0).getBrowserDownloadUrl();
                }

                return List.of(releaseLink, downloadLink, version);
            }

            List<GHAsset> assets = release.listAssets().toList();

            if (assets.isEmpty())
            {
                return List.of();
            }

            version = ghVersionTag;
            releaseLink = String.valueOf(release.getHtmlUrl());
            downloadLink = assets.get(0).getBrowserDownloadUrl();
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
        }

        return List.of(releaseLink, downloadLink, version);
    }

    public File downloadAndInstallFile(TextArea detailArea, List<String> versionData)
    {
        File file = null;

        try
        {
            String downloadUrl = versionData.get(1);

            if (!downloadUrl.matches("^ht{2}ps://github[.]com/BlockyDotJar/VorteX/releases/download/v\\d+[.]\\d+[.]\\d+/VorteX-Setup-\\d+[.]\\d+[.]\\d+[.]exe"))
            {
                return null;
            }

            String vortexHome = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_HOME");

            if (vortexHome == null)
            {
                vortexHome = System.getProperty("ProgramFiles(x86)") + "\\VorteX";
            }

            File vortex = new File(vortexHome);

            if (!vortex.exists() || !vortex.isDirectory())
            {
                return null;
            }

            File update = new File(vortex, "update");

            if (!update.exists() || !update.isDirectory())
            {
                update.mkdir();
            }

            String exeName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);

            file = new File(update, exeName);
            URL url = new URL(downloadUrl);

            FileUtils.copyURLToFile(url, file);
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
        }
        return file;
    }

    public void startDownloadTask(HostServices hostServices, NodeCreator creator, TextArea detailArea, List<String> versionDetails)
    {
        final File[] installer = {null};

        Task<Void> backgroundTask = new Task<>()
        {
            @Override
            protected Void call()
            {
                installer[0] = downloadAndInstallFile(detailArea, versionDetails);
                return null;
            }
        };

        backgroundTask.setOnSucceeded((e) ->
                Platform.runLater(() ->
                {
                    String title = "Successfully downloaded newest version!";
                    String headerText = "VorteX will now be closed, and the installation wizard will be opened.";

                    Alert closeAlert = creator.createAlert(Alert.AlertType.INFORMATION, title, headerText, null);

                    closeAlert.showAndWait();

                    hostServices.showDocument(installer[0].getAbsolutePath());

                    System.exit(0);
                })
        );

        new Thread(backgroundTask).start();
    }
}
