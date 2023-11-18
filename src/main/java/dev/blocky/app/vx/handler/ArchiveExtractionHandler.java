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
package dev.blocky.app.vx.handler;

import dev.blocky.app.vx.codec.Base32Codec;
import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.windows.api.WindowsExplorer;
import javafx.application.HostServices;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.PopOver;

import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static dev.blocky.app.vx.handler.ActionHandler.*;
import static dev.blocky.app.vx.handler.ArchiveCreationHandler.initShowPasswordCheck;

public class ArchiveExtractionHandler
{
    private static final NodeCreator creator = new NodeCreator();

    private static final HashSet<FileHeader> filesToExtract = new HashSet<>();

    private static PopOver extractionPreview;
    private static File fileToExtract;

    public static void initExtractArchive(Stage stage, HostServices hostServices, AnchorPane anchorPane, TextArea detailArea, Button extractArchive)
    {
        extractArchive.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = extractArchive;
            extractArchive.setDisable(true);

            anchorPane.getChildren().clear();

            Button chooseArchive = creator.createButton("Choose archive", 10, 170, 150, false);
            Button extract = creator.createButton("Extract", 180, 170, 100, true);
            Button clear = creator.createButton("Clear", 335, 170, -1, false);

            PasswordField password = creator.createPasswordField("Enter the password of the archive", 10, 220);

            password.textProperty().addListener((obs, oldVal, newVal) ->
            {
                if (fileToExtract == null)
                {
                    extract.setDisable(true);
                    return;
                }

                if (newVal.isBlank())
                {
                    extract.setDisable(true);
                    return;
                }

                extract.setDisable(false);
            });

            TextField passwordUnmasked = creator.createTextField("Enter the password of the archive", 10, 220, -1, false, false, true);

            CheckBox showPassword = creator.createCheckBox("Show password", 460, 224);

            anchorPane.getChildren().addAll(detailArea, password, passwordUnmasked, showPassword, chooseArchive, extract, clear);

            initShowPasswordCheck(showPassword, password, passwordUnmasked);
            initChooseArchive(stage, detailArea, chooseArchive, extract, password);
            initExtract(stage, hostServices, detailArea, extract, password);
            initClear(detailArea, clear, extract, password, showPassword);
        });
    }

    public static void initChooseArchive(Stage stage, TextArea detailArea, Button chooseArchive, Button extract, PasswordField password)
    {
        chooseArchive.setOnAction(event ->
        {
            try
            {
                List<FileChooser.ExtensionFilter> extensionFilter = Collections.singletonList(new FileChooser.ExtensionFilter("VorteX Archive", "*.vxar"));

                FileChooser fileChooser = creator.createFileChooser("Choose a archive", null, null, extensionFilter);

                File file = fileChooser.showOpenDialog(stage);

                if (file == null)
                {
                    if (fileToExtract == null || !fileToExtract.exists())
                    {
                        extract.setDisable(true);
                    }
                    return;
                }

                validAction(detailArea, "Selected '" + file.getName() + "' for extraction.");

                fileToExtract = file;

                ZipFile zipFile = new ZipFile(fileToExtract);

                initExtractionPreview(zipFile, detailArea, extract);

                if (!password.getText().isBlank())
                {
                    extract.setDisable(false);
                }
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public static void initExtract(Stage stage, HostServices hostServices, TextArea detailArea, Button extract, PasswordField password)
    {
        extract.setOnAction(event ->
        {
            try
            {
                if (password.getText().isBlank())
                {
                    invalidAction(detailArea, "Please enter a password to continue.");
                    return;
                }

                DirectoryChooser directoryChooser = creator.createDirectoryChooser("Save as...");

                File extractIn = directoryChooser.showDialog(stage);

                if (extractIn == null)
                {
                    return;
                }

                validAction(detailArea, "File '" + fileToExtract.getName() + "' will be extracted in directory " + extractIn.getAbsolutePath());
                validAction(detailArea, "Extraction started...");

                ZipFile zipFile = new ZipFile(fileToExtract, password.getText().toCharArray());

                Base32Codec.decodeAndExtractFile(zipFile, filesToExtract, extractIn.getAbsolutePath());

                int fileCount = 0;
                int directoryCount = 0;

                for (FileHeader fileHeader : zipFile.getFileHeaders())
                {
                    if (FilenameUtils.getExtension(fileHeader.getFileName()).isBlank())
                    {
                        directoryCount++;
                        continue;
                    }

                    fileCount++;
                }

                String text = "Successfully extracted " + fileCount + " files and " + directoryCount + " directories from '" + fileToExtract.getName() + "' to " + extractIn.getAbsolutePath();

                validAction(detailArea, text);

                extract.setDisable(true);

                fileToExtract = null;

                SystemTray tray = SystemTray.getSystemTray();

                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                TrayIcon trayIcon = new TrayIcon(image);
                trayIcon.setImageAutoSize(true);
                trayIcon.addActionListener((e) -> hostServices.showDocument(extractIn.getAbsolutePath()));

                tray.add(trayIcon);

                String caption = "Successfully extracted '" + zipFile.getFile().getName() + "'";

                trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);

                TimeUnit.MILLISECONDS.sleep(500);

                tray.remove(trayIcon);
            }
            catch (Exception e)
            {
                if (e instanceof ZipException zipE)
                {
                    invalidAction(detailArea, zipE.getMessage() + ".");
                    return;
                }

                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public static void initExtractionPreview(ZipFile zipFile, TextArea detailArea, Button extract) throws ZipException
    {
        AnchorPane anchorPane = new AnchorPane();

        CheckBoxTreeItem<String> rootItem = new CheckBoxTreeItem<>();

        for (FileHeader fileHeader : zipFile.getFileHeaders())
        {
            filesToExtract.add(fileHeader);

            String[] parts = fileHeader.getFileName().split("/");

            CheckBoxTreeItem<String> currentItem = rootItem;

            for (String part : parts)
            {
                CheckBoxTreeItem<String> foundChild = null;

                String decodedPart = Base32Codec.decodeEncodedPart(part);

                for (TreeItem<String> child : currentItem.getChildren())
                {
                    if (child.getValue().equals(decodedPart))
                    {
                        foundChild = (CheckBoxTreeItem<String>) child;
                        break;
                    }
                }

                if (foundChild == null)
                {
                    CheckBoxTreeItem<String> newChild = new CheckBoxTreeItem<>(decodedPart);

                    currentItem.getChildren().add(newChild);

                    currentItem = newChild;
                    currentItem.setSelected(true);
                    currentItem.setIndependent(true);

                    currentItem.selectedProperty().addListener((obs, oldVal, newVal) ->
                    {
                        String text = detailArea.getText();
                        String[] lines = text.split("\n");

                        if (!newVal)
                        {
                            filesToExtract.remove(fileHeader);

                            if (filesToExtract.isEmpty())
                            {
                                extract.setDisable(true);
                            }

                            String validText = "Removed '" + decodedPart + "' from extraction list.";

                            if (!lines[lines.length - 1].equals(validText))
                            {
                                validAction(detailArea, validText);
                            }
                            return;
                        }

                        filesToExtract.add(fileHeader);

                        String validText = "Added '" + decodedPart + "' to extraction list.";

                        if (!lines[lines.length - 1].equals(validText))
                        {
                            validAction(detailArea, validText);
                        }

                        extract.setDisable(false);
                    });
                    continue;
                }

                currentItem = foundChild;
                currentItem.setSelected(true);
                currentItem.setIndependent(false);

                currentItem.selectedProperty().addListener((obs, oldVal, newVal) ->
                {
                    String text = detailArea.getText();
                    String[] lines = text.split("\n");

                    if (!newVal)
                    {
                        filesToExtract.remove(fileHeader);

                        if (filesToExtract.isEmpty())
                        {
                            extract.setDisable(true);
                        }

                        String validText = "Removed '" + decodedPart + "' from extraction list.";

                        if (!lines[lines.length - 1].equals(validText))
                        {
                            validAction(detailArea, validText);
                        }
                        return;
                    }

                    filesToExtract.add(fileHeader);

                    String validText = "Added '" + decodedPart + "' to extraction list.";

                    if (!lines[lines.length - 1].equals(validText))
                    {
                        validAction(detailArea, validText);
                    }

                    extract.setDisable(false);
                });
            }
        }

        CheckTreeView<String> checkTreeView = new CheckTreeView<>(rootItem);
        checkTreeView.setShowRoot(false);
        checkTreeView.setPrefWidth(400);

        checkTreeView.setCellFactory(tv -> new CheckBoxTreeCell<>()
        {
            @Override
            public void updateItem(String value, boolean empty)
            {
                super.updateItem(value, empty);

                setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
            }
        });

        anchorPane.getChildren().add(checkTreeView);

        extractionPreview = creator.createPopOver(anchorPane);

        extract.setOnMouseEntered(event ->
        {
            if (!extractionPreview.isShowing())
            {
                extractionPreview.show(extract);
            }
        });

        AtomicReference<Point2D> dragStartLocation = new AtomicReference<>();

        extract.setOnMouseExited(event ->
        {
            ScrollBar scrollbarV = null;
            ScrollBar scrollbarH = null;

            for (Node node : checkTreeView.lookupAll(".scroll-bar"))
            {
                if (node instanceof ScrollBar sb)
                {
                    if (sb.getOrientation() == Orientation.VERTICAL)
                    {
                        scrollbarV = sb;
                        continue;
                    }
                    scrollbarH = sb;
                }
            }

            ScrollBar finalScrollbarV = scrollbarV;
            ScrollBar finalScrollbarH = scrollbarH;

            anchorPane.setOnMouseExited(eme ->
            {
                if (!finalScrollbarV.isVisible() && !finalScrollbarH.isVisible())
                {
                    extractionPreview.hide();
                    return;
                }

                if (dragStartLocation.get() == null && extractionPreview.isShowing())
                {
                    extractionPreview.hide();
                }
            });

            finalScrollbarV.lookup(".thumb").setOnMousePressed(em -> dragStartLocation.set(new Point2D(em.getScreenX(), em.getScreenY())));
            finalScrollbarV.lookup(".thumb").setOnMouseReleased(emr -> dragStartLocation.set(null));

            finalScrollbarH.lookup(".thumb").setOnMousePressed(em -> dragStartLocation.set(new Point2D(em.getScreenX(), em.getScreenY())));
            finalScrollbarH.lookup(".thumb").setOnMouseReleased(emr -> dragStartLocation.set(null));
        });
    }

    public static void initClear(TextArea detailArea, Button clear, Button extract, PasswordField password, CheckBox showPassword)
    {
        clear.setOnAction(event ->
        {
            detailArea.clear();

            extract.setDisable(true);

            password.clear();

            showPassword.setSelected(false);

            fileToExtract = null;
        });
    }
}
