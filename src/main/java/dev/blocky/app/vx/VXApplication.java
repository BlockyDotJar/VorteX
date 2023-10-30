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

import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.updater.ApplicationUpdater;
import dev.blocky.app.vx.windows.api.DwmAttribute;
import dev.blocky.app.vx.windows.api.Win11Style;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.HiddenSidesPane;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static dev.blocky.app.vx.handler.ActionHandler.lastUsedButton;
import static dev.blocky.app.vx.handler.ArchiveCreationHandler.*;
import static dev.blocky.app.vx.handler.ArchiveExtractionHandler.initExtractArchive;
import static dev.blocky.app.vx.handler.ArchiveOpeningHandler.initOpenArchive;
import static dev.blocky.app.vx.handler.BarcodeCreationHandler.initCreateBarcode;
import static dev.blocky.app.vx.handler.BarcodeReadingHandler.initReadBarcode;
import static dev.blocky.app.vx.handler.DocumentConversionHandler.initConvertDocument;
import static dev.blocky.app.vx.handler.PDFConversionHandler.initConvertPDF;

public class VXApplication extends Application
{
    private static String[] args;

    @Override
    public void start(Stage stage) throws IOException
    {
        if (args.length > 1)
        {
            System.exit(0);
            return;
        }

        NodeCreator creator = new NodeCreator();

        HiddenSidesPane hiddenSidesPane = new HiddenSidesPane();
        hiddenSidesPane.setTriggerDistance(25);

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getStyleClass().add("anchor-pane");

        TextArea detailArea = creator.createTextArea(null, 10, 415);

        Button createArchive = creator.createButton("Create Archive", 10, 10, 125, false);
        Button openArchive = creator.createButton("Open Archive", 140, 10, 125, false);
        Button extractArchive = creator.createButton("Extract Archive", 270, 10, 125, false);
        Button convertDocument = creator.createButton("Convert Document", 10, 55, 150, false);
        Button convertPDF = creator.createButton("Convert PDF", 165, 55, 115, false);
        Button createBarcode = creator.createButton("Create Barcode", 10, 100, 125, false);
        Button readBarcode = creator.createButton("Read Barcode", 140, 100, 125, false);

        if (args.length == 0)
        {
            Button addFile = creator.createButton("Add File", 10, 10, -1, false);
            Button addFolder = creator.createButton("Add Folder", 120, 10, -1, false);
            Button create = creator.createButton("Create", 335, 10, -1, true);
            Button clear = creator.createButton("Clear", 460, 10, -1, false);

            PasswordField password = creator.createPasswordField("Enter a password", 10, 65);
            PasswordField passwordCheck = creator.createPasswordField("Confirm password", 10, 105);

            password.textProperty().addListener((obs, oldVal, newVal) ->
            {
                if (chosenFiles.isEmpty() && chosenDirectories.isEmpty())
                {
                    create.setDisable(true);
                    return;
                }

                if (newVal.isBlank())
                {
                    create.setDisable(true);
                    return;
                }

                passwordCheck.textProperty().addListener((obsCheck, oldValCheck, newValCheck) ->
                {
                    if (newValCheck.isBlank())
                    {
                        create.setDisable(true);
                        return;
                    }

                    create.setDisable(false);
                });
            });

            TextField passwordUnmasked = creator.createTextField("Enter a password", 10, 65, -1, false, false, true);
            TextField passwordCheckUnmasked = creator.createTextField("Confirm password", 10, 105, -1, false, false, true);

            CheckBox showPassword = creator.createCheckBox("Show password", 460, 69);
            CheckBox showPasswordCheck = creator.createCheckBox("Show password", 460, 109);

            ObservableList<CompressionLevel> compressionLevels = FXCollections.observableArrayList
                    (
                            CompressionLevel.NO_COMPRESSION, CompressionLevel.FASTEST, CompressionLevel.FASTER, CompressionLevel.FAST,
                            CompressionLevel.MEDIUM_FAST, CompressionLevel.NORMAL, CompressionLevel.HIGHER, CompressionLevel.MAXIMUM,
                            CompressionLevel.PRE_ULTRA, CompressionLevel.ULTRA
                    );

            ComboBox<CompressionLevel> compressionLevel = creator.createComboBox("NORMAL", 10, 155, -1, compressionLevels);

            TextField comment = creator.createTextField("Set comment for archive file", 10, 205, -1, true, true, true);

            ObservableList<EncryptionMethod> encryptionMethods = FXCollections.observableArrayList
                    (
                            EncryptionMethod.ZIP_STANDARD, EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG, EncryptionMethod.AES
                    );

            ComboBox<EncryptionMethod> encryptionMethod = creator.createComboBox("AES", 10, 255, 80, encryptionMethods);

            ToggleGroup aesGroup = new ToggleGroup();

            RadioButton aes1 = creator.createRadioButton("AES-1", aesGroup, 10, 302);
            RadioButton aes2 = creator.createRadioButton("AES-2", aesGroup, 100, 302);

            ObservableList<AesKeyStrength> aesKeyStrengths = FXCollections.observableArrayList
                    (
                            AesKeyStrength.KEY_STRENGTH_128, AesKeyStrength.KEY_STRENGTH_192, AesKeyStrength.KEY_STRENGTH_256
                    );

            ComboBox<AesKeyStrength> aesKeyStrength = creator.createComboBox("KEY_STRENGTH_256", 10, 355, 205, aesKeyStrengths);

            anchorPane.getChildren().addAll(addFile, addFolder, create, clear, password, passwordCheck, passwordUnmasked, passwordCheckUnmasked, showPassword, showPasswordCheck, compressionLevel, comment, encryptionMethod, aesKeyStrength, aes1, aes2, detailArea);

            initAddFile(stage, detailArea, addFile, create, password, passwordCheck);
            initAddFolder(stage, detailArea, addFolder, create, password, passwordCheck);
            initCreate(stage, detailArea, create, password, passwordCheck, comment);
            initClear(detailArea, clear, create, password, passwordCheck, showPassword, showPasswordCheck, compressionLevel, comment, encryptionMethod, aes1, aes2, aesKeyStrength);
            initShowPassword(showPassword, password, passwordUnmasked);
            initShowPasswordCheck(showPasswordCheck, passwordCheck, passwordCheckUnmasked);
            initCompressionLevel(compressionLevel);
            initEncryptionMethod(encryptionMethod, aesKeyStrength, aes1, aes2);
            initAESKeyStrength(aesKeyStrength);
            initAESVersion(aesGroup, aes1);

            createArchive = creator.createButton("Create Archive", 10, 10, 125, true);
            lastUsedButton = createArchive;
        }

        if (args.length == 1)
        {
            File file = new File(args[0]);

            if (!file.exists() || !file.isFile() || file.isDirectory() || !FilenameUtils.getExtension(file.getName()).equals("vxar"))
            {
                System.exit(0);
                return;
            }

            openArchive = creator.createButton("Open Archive", 140, 10, 125, true);
            lastUsedButton = openArchive;
        }

        hiddenSidesPane.setContent(anchorPane);

        Pane hiddenPane = new Pane();
        hiddenPane.getStyleClass().add("hidden-pane");
        hiddenPane.setPrefHeight(140);

        hiddenPane.setOnMouseEntered(e -> hiddenSidesPane.setPinnedSide(Side.BOTTOM));
        hiddenPane.setOnMouseExited(e -> hiddenSidesPane.setPinnedSide(null));

        hiddenPane.getChildren().addAll(createArchive, openArchive, extractArchive, convertDocument, convertPDF, createBarcode, readBarcode);

        hiddenSidesPane.setBottom(hiddenPane);

        Image icon = new Image(getClass().getResource("/assets/icons/icon.png").openStream());

        Scene scene = new Scene(hiddenSidesPane, 605, 525);
        scene.getStylesheets().add(getClass().getResource("/assets/ui/css/styles.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle("VorteX");
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        HostServices hostServices = getHostServices();

        initCreateArchive(stage, anchorPane, detailArea, createArchive);
        initOpenArchive(anchorPane, detailArea, openArchive);
        initExtractArchive(stage, hostServices, anchorPane, detailArea, extractArchive);
        initConvertDocument(stage, hostServices, anchorPane, detailArea, convertDocument);
        initConvertPDF(stage, hostServices, anchorPane, detailArea, convertPDF);
        initCreateBarcode(stage, anchorPane, detailArea, createBarcode);
        initReadBarcode(stage, anchorPane, detailArea, readBarcode);

        String script = IOUtils.toString(getClass().getResource("/assets/scripts/webEngine.js"), StandardCharsets.UTF_8);

        Platform.runLater(() ->
        {
            Win11Style.WindowHandle handle = Win11Style.findWindowHandle(stage);
            Win11Style.dwmSetBooleanValue(handle, DwmAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, true);

            if (!Win11Style.dwmSetIntValue(handle, DwmAttribute.DWMWA_SYSTEMBACKDROP_TYPE, DwmAttribute.DWMSBT_MAINWINDOW.value))
            {
                Win11Style.dwmSetBooleanValue(handle, DwmAttribute.DWMWA_MICA_EFFECT, true);
            }

            ApplicationUpdater updater = new ApplicationUpdater();
            List<String> versionDetails = updater.newVersion(detailArea);

            if (!versionDetails.isEmpty())
            {
                String version = versionDetails.get(2);
                String releaseLink = versionDetails.get(0);

                AnchorPane alertPane = new AnchorPane();
                alertPane.setMinSize(705, 435);
                alertPane.setMaxSize(705, 435);

                String text = "Version " + version + " is here! Do you want to install the newest version of VorteX?";

                Label label = creator.createLabel(text, 4, 0);
                Hyperlink hyperlink = creator.createHyperlink(hostServices, "Read here about the new version.", releaseLink, 0, 15);

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
                                    final File[] installer = {null};

                                    Task<Void> backgroundTask = new Task<>()
                                    {
                                        @Override
                                        protected Void call()
                                        {
                                            installer[0] = updater.downloadAndInstallFile(detailArea, versionDetails);
                                            return null;
                                        }
                                    };

                                    backgroundTask.setOnSucceeded((e) ->
                                            Platform.runLater(() ->
                                            {
                                                String closeTitle = "Successfully downloaded newest version!";
                                                String closeHeaderText = "VorteX will now be closed, and the installation wizard will be opened.";

                                                Alert closeAlert = creator.createAlert(Alert.AlertType.INFORMATION, closeTitle, closeHeaderText, null);

                                                closeAlert.showAndWait();

                                                hostServices.showDocument(installer[0].getAbsolutePath());

                                                System.exit(0);
                                            })
                                    );

                                    new Thread(backgroundTask).start();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public static void main(String[] args)
    {
        VXApplication.args = args;

        launch();
    }
}
