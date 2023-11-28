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
package dev.blocky.app.vx;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.windows.api.dwm.DWMAttribute;
import dev.blocky.app.vx.windows.api.dwm.DWMHandler;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.HiddenSidesPane;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.blocky.app.vx.handler.ActionHandler.lastUsedButton;
import static dev.blocky.app.vx.handler.ArchiveCreationHandler.initCreateArchive;
import static dev.blocky.app.vx.handler.ArchiveCreationHandler.initRoot;
import static dev.blocky.app.vx.handler.ArchiveExtractionHandler.initExtractArchive;
import static dev.blocky.app.vx.handler.ArchiveOpeningHandler.initOpenArchive;
import static dev.blocky.app.vx.handler.BarcodeCreationHandler.initCreateBarcode;
import static dev.blocky.app.vx.handler.BarcodeReadingHandler.initReadBarcode;
import static dev.blocky.app.vx.handler.SettingHandler.initSettings;
import static dev.blocky.app.vx.handler.SettingHandler.updateCheck;
import static dev.blocky.app.vx.updater.ApplicationUpdater.initApplicationUpdater;

public class VXApplication extends Application
{
    private static String[] args;

    @Override
    public void start(Stage stage) throws IOException
    {
        NodeCreator creator = new NodeCreator();

        if (args.length > 1)
        {
            String title = "Error while opening VorteX...";
            String headerText = "Multi file selection detected...";
            String contentText =
                    """
                            You can't open more then one files a time.
                            VorteX will be closed after button interaction...
                            """;

            Alert closeAlert = creator.createAlert(Alert.AlertType.ERROR, title, headerText, contentText);
            closeAlert.showAndWait();

            System.exit(0);
            return;
        }

        HostServices hostServices = getHostServices();

        if (!SystemUtils.IS_OS_WINDOWS)
        {
            String title = "Error while opening VorteX...";
            String headerText = "Unsupported operating system detected...";

            AnchorPane alertPane = new AnchorPane();

            Label errorLabel = creator.createLabel("You are trying to open this application on an operating system that isn't supported.", 4, 0);
            Hyperlink hyperlink = creator.createHyperlink(hostServices, "Read here about supported platforms.", "https://github.com/BlockyDotJar/VorteX#supported-platforms", 0, 10);
            Label closeLabel = creator.createLabel("VorteX will be closed after button interaction...", 4, 25);

            alertPane.getChildren().addAll(errorLabel, hyperlink, closeLabel);

            Alert closeAlert = creator.createAlert(Alert.AlertType.ERROR, title, headerText, alertPane);
            closeAlert.showAndWait();

            System.exit(0);
            return;
        }

        HiddenSidesPane hiddenSidesPane = new HiddenSidesPane();
        hiddenSidesPane.setTriggerDistance(25);

        AnchorPane anchorPane = new AnchorPane();

        TextArea detailArea = creator.createTextArea(null, 10, 415);

        Button createArchive = creator.createButton("Create Archive", 10, 10, 125, false);
        Button openArchive = creator.createButton("Open Archive", 140, 10, 125, false);
        Button extractArchive = creator.createButton("Extract Archive", 270, 10, 125, false);
        Button createBarcode = creator.createButton("Create Barcode", 10, 50, 125, false);
        Button readBarcode = creator.createButton("Read Barcode", 140, 50, 125, false);

        Button settings = creator.createButton("\u2699", 565, 50, 30, false);

        if (args.length == 0)
        {
            createArchive = creator.createButton("Create Archive", 10, 10, 125, true);
            lastUsedButton = createArchive;
            initRoot(stage, anchorPane, detailArea, createArchive);
        }

        if (args.length == 1)
        {
            File file = new File(args[0]);

            if (!file.exists() || !file.isFile() || file.isDirectory() || !FilenameUtils.getExtension(file.getName()).equals("vxar"))
            {
                String title = "Error while opening VorteX...";
                String headerText = "Unknown error detected...";
                String contentText = String.format(
                        """
                                A weird error occurred on application start.

                                Here are some parameter that can help you finding the issue:
                                > file.exists = %b
                                > file.isFile = %b
                                > file.isDirectory = %b
                                > file.isVorteXArchive = %b
                                    
                                VorteX will be closed after button interaction...
                                """,
                        file.exists(), file.isFile(), file.isDirectory(), FilenameUtils.getExtension(file.getName()).equals("vxar")
                );

                Alert closeAlert = creator.createAlert(Alert.AlertType.ERROR, title, headerText, contentText);
                closeAlert.showAndWait();

                System.exit(0);
                return;
            }

            openArchive = creator.createButton("Open Archive", 140, 10, 125, true);
            lastUsedButton = openArchive;
        }

        hiddenSidesPane.setContent(anchorPane);

        Pane hiddenPane = new Pane();
        hiddenPane.getStyleClass().add("hidden-pane");
        hiddenPane.setPrefHeight(90);

        hiddenPane.setOnMouseEntered(e -> hiddenSidesPane.setPinnedSide(Side.BOTTOM));
        hiddenPane.setOnMouseExited(e -> hiddenSidesPane.setPinnedSide(null));

        hiddenPane.getChildren().addAll(createArchive, openArchive, extractArchive, createBarcode, readBarcode, settings);

        hiddenSidesPane.setBottom(hiddenPane);

        Image icon = new Image(getClass().getResource("/assets/icons/icon.png").openStream());

        String vortexHome = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_HOME");
        File settingsFile = new File(vortexHome + "\\settings.json");
        String jsonInput = Files.readString(settingsFile.toPath());

        String cssFilename = "styles";

        JSONObject root = new JSONObject(jsonInput);

        boolean defaultDarkMode = root.getBoolean("default-dark-mode");

        JSONObject dwm = root.getJSONObject("dwm");

        int windowType = dwm.getInt("window-type");
        boolean immersiveDarkMode = dwm.getBoolean("immersive-dark-mode");

        if (windowType == 1 && !defaultDarkMode)
        {
            cssFilename = "styles";
        }

        if (windowType == 1 && defaultDarkMode)
        {
            cssFilename = "dark-styles";
        }

        if ((windowType == 2 || windowType == 4) && !immersiveDarkMode)
        {
            cssFilename = "dwm-styles";
        }

        if ((windowType == 2 || windowType == 4) && immersiveDarkMode)
        {
            cssFilename = "dwm-dark-styles";
        }

        Scene scene = new Scene(hiddenSidesPane, 605, 525);
        scene.getStylesheets().add(getClass().getResource("/assets/ui/css/" + cssFilename + ".css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle("VorteX");
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.show();

        initCreateArchive(stage, anchorPane, detailArea, createArchive);
        initOpenArchive(anchorPane, detailArea, openArchive);
        initExtractArchive(stage, hostServices, anchorPane, detailArea, extractArchive);
        initCreateBarcode(stage, anchorPane, detailArea, createBarcode);
        initReadBarcode(stage, anchorPane, detailArea, readBarcode);

        String script = IOUtils.toString(getClass().getResource("/assets/scripts/webEngine.js"), StandardCharsets.UTF_8);

        initSettings(hostServices, scene, anchorPane, detailArea, script, settings);

        Platform.runLater(() ->
        {
            String displayVersion = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", "DisplayVersion");
            int dvNumber = Integer.parseInt(StringUtils.remove(displayVersion, "H"));

            if (SystemUtils.IS_OS_WINDOWS_11 && dvNumber >= 222)
            {
                if (windowType == 2 || windowType == 4)
                {
                    DWMAttribute dwmAttribute = DWMAttribute.findAttribute(windowType);
                    DWMHandler.setMicaStyle(dwmAttribute, immersiveDarkMode);
                }

                DWMHandler.handleStyleSettings(dwm);
            }

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            executor.schedule(() ->
                    Platform.runLater(() ->
                    {
                        if (updateCheck)
                        {
                            initApplicationUpdater(hostServices, detailArea, script);
                        }
                    }), 5, TimeUnit.SECONDS
            );

            executor.shutdown();
        });
    }

    public static void main(String[] args)
    {
        VXApplication.args = args;

        System.setProperty("prism.forceUploadingPainter", "true");

        launch();
    }
}
