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
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kohsuke.github.*;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import static dev.blocky.app.vx.handler.ActionHandler.invalidAction;
import static dev.blocky.app.vx.handler.TrayIconHandler.sendErrorPushNotification;

public class ApplicationUpdater
{
    private static String getCurrentVersion()
    {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_Version");
    }

    private static boolean isOlderVersion(String currentVersion, String latestVersion)
    {
        Comparator<String> versionComparator = Comparator.comparing(s -> s, (cv, lv) ->
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

    private static List<String> getLatestVersion(TextArea detailArea)
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
            sendErrorPushNotification(detailArea, e);
        }

        return List.of(releaseLink, downloadLink, version);
    }

    private static File downloadAndInstallFile(TextArea detailArea, List<String> versionData)
    {
        File file = null;

        try
        {
            String downloadUrl = versionData.get(1);

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
            sendErrorPushNotification(detailArea, e);
        }
        return file;
    }

    private static void startDownloadTask(HostServices hostServices, NodeCreator creator, TextArea detailArea, List<String> versionDetails)
    {
        Task<File> backgroundTask = new Task<>()
        {
            @Override
            protected File call()
            {
                return downloadAndInstallFile(detailArea, versionDetails);
            }
        };

        backgroundTask.setOnSucceeded((e) ->
                Platform.runLater(() ->
                {
                    String title = "Successfully downloaded newest version!";
                    String headerText = "Installation wizard was downloaded successfully!";
                    String contentText = "VorteX will be closed after button interaction and the installation wizard will be opened.";

                    Alert closeAlert = creator.createAlert(Alert.AlertType.INFORMATION, title, headerText, contentText);

                    closeAlert.showAndWait();

                    hostServices.showDocument(backgroundTask.getValue().getAbsolutePath());

                    System.exit(0);
                })
        );

        new Thread(backgroundTask).start();
    }

    public static boolean initApplicationUpdater(HostServices hostServices, TextArea detailArea, String script)
    {
        NodeCreator creator = new NodeCreator();
        List<String> versionDetails = getLatestVersion(detailArea);

        if (!versionDetails.isEmpty())
        {
            String version = versionDetails.get(2);
            String releaseLink = versionDetails.get(0);

            AnchorPane alertPane = new AnchorPane();
            alertPane.setMinSize(705, 435);
            alertPane.setMaxSize(705, 435);

            String text = "Version " + version + " is here! Do you want to install the newest version of VorteX?";

            Label label = creator.createLabel(text, 20, 0);
            Hyperlink hyperlink = creator.createHyperlink(hostServices, "Read here about the new version.", releaseLink, 16, 15);

            WebView webView = creator.createWebView(20, 40);
            WebEngine webEngine = webView.getEngine();
            webEngine.load(releaseLink);

            alertPane.getChildren().addAll(label, hyperlink, webView);

            String title = "New VorteX version available!";
            String headerText = "Looks like there is a new version of VorteX (" + version + ") available!";

            Alert updateAlert = creator.createAlert(Alert.AlertType.CONFIRMATION, title, headerText, alertPane);

            ButtonType downloadButton = new ButtonType("Download", ButtonBar.ButtonData.OK_DONE);
            updateAlert.getButtonTypes().setAll(downloadButton, ButtonType.CANCEL);

            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
            {
                if (newValue == Worker.State.SCHEDULED)
                {
                    webView.setVisible(false);
                }

                if (newValue == Worker.State.SUCCEEDED)
                {
                    webEngine.executeScript(script);

                    webView.setVisible(true);

                    if (!updateAlert.isShowing())
                    {
                        updateAlert.showAndWait().ifPresent((bt) ->
                        {
                            if (bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            {
                                startDownloadTask(hostServices, creator, detailArea, versionDetails);
                            }
                        });
                    }
                }
            });
            return true;
        }
        return false;
    }
}
