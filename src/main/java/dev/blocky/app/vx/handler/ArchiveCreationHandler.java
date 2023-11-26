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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.AesVersion;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.PopOver;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static dev.blocky.app.vx.handler.ActionHandler.*;

public class ArchiveCreationHandler
{
    public static final HashSet<File> chosenFiles = new HashSet<>(), chosenDirectories = new HashSet<>();

    private static final NodeCreator creator = new NodeCreator();

    private static AesKeyStrength aesks = AesKeyStrength.KEY_STRENGTH_256;
    private static CompressionLevel cpl = CompressionLevel.NORMAL;
    private static EncryptionMethod em = EncryptionMethod.AES;
    private static AesVersion aesv = AesVersion.TWO;

    private static PopOver creationPreview;

    public static void initRoot(Stage stage, AnchorPane anchorPane, TextArea detailArea)
    {
        anchorPane.getChildren().clear();

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

            if (passwordCheck.getText().isBlank())
            {
                create.setDisable(true);
                return;
            }

            if (!newVal.equals(passwordCheck.getText()))
            {
                create.setDisable(true);
                return;
            }

            create.setDisable(false);
        });

        passwordCheck.textProperty().addListener((obsCheck, oldValCheck, newValCheck) ->
        {
            if (chosenFiles.isEmpty() && chosenDirectories.isEmpty())
            {
                create.setDisable(true);
                return;
            }

            if (newValCheck.isBlank())
            {
                create.setDisable(true);
                return;
            }

            if (password.getText().isBlank())
            {
                create.setDisable(true);
                return;
            }

            if (!password.getText().equals(newValCheck))
            {
                create.setDisable(true);
                return;
            }

            create.setDisable(false);
        });

        TextField passwordUnmasked = creator.createTextField("Enter a password", null, 10, 65, -1, false, false, true, false);
        TextField passwordCheckUnmasked = creator.createTextField("Confirm password", null, 10, 105, -1, false, false, true, false);

        CheckBox showPassword = creator.createCheckBox("Show password", 460, 69);
        CheckBox showPasswordCheck = creator.createCheckBox("Show password", 460, 109);

        ObservableList<CompressionLevel> compressionLevels = FXCollections.observableArrayList
                (
                        CompressionLevel.NO_COMPRESSION, CompressionLevel.FASTEST, CompressionLevel.FASTER, CompressionLevel.FAST,
                        CompressionLevel.MEDIUM_FAST, CompressionLevel.NORMAL, CompressionLevel.HIGHER, CompressionLevel.MAXIMUM,
                        CompressionLevel.PRE_ULTRA, CompressionLevel.ULTRA
                );

        ComboBox<CompressionLevel> compressionLevel = creator.createComboBox("NORMAL", 10, 155, -1, compressionLevels, false);

        TextField comment = creator.createTextField("Set comment for archive file", null, 10, 205, -1, true, true, true, false);

        ObservableList<EncryptionMethod> encryptionMethods = FXCollections.observableArrayList
                (
                        EncryptionMethod.ZIP_STANDARD, EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG, EncryptionMethod.AES
                );

        ComboBox<EncryptionMethod> encryptionMethod = creator.createComboBox("AES", 10, 255, 80, encryptionMethods, false);

        ToggleGroup aesGroup = new ToggleGroup();

        RadioButton aes1 = creator.createRadioButton("AES-1", aesGroup, 10, 302);
        RadioButton aes2 = creator.createRadioButton("AES-2", aesGroup, 100, 302);

        ObservableList<AesKeyStrength> aesKeyStrengths = FXCollections.observableArrayList
                (
                        AesKeyStrength.KEY_STRENGTH_128, AesKeyStrength.KEY_STRENGTH_192, AesKeyStrength.KEY_STRENGTH_256
                );

        ComboBox<AesKeyStrength> aesKeyStrength = creator.createComboBox("KEY_STRENGTH_256", 10, 355, 205, aesKeyStrengths, false);

        anchorPane.getChildren().addAll(addFile, addFolder, create, clear, password, passwordCheck, passwordUnmasked, passwordCheckUnmasked, showPassword, showPasswordCheck, compressionLevel, comment, encryptionMethod, aesKeyStrength, aes1, aes2, detailArea);

        initAddFile(stage, detailArea, addFile, create, password, passwordCheck);
        initAddFolder(stage, detailArea, addFolder, create, password, passwordCheck);
        initCreate(stage, detailArea, create, password, comment);
        initClear(detailArea, clear, create, password, passwordCheck, showPassword, showPasswordCheck, compressionLevel, comment, encryptionMethod, aes1, aes2, aesKeyStrength);
        initShowPassword(showPassword, password, passwordUnmasked);
        initShowPasswordCheck(showPasswordCheck, passwordCheck, passwordCheckUnmasked);
        initCompressionLevel(compressionLevel);
        initEncryptionMethod(encryptionMethod, aesKeyStrength, aes1, aes2);
        initAESKeyStrength(aesKeyStrength);
        initAESVersion(aesGroup, aes1);
    }

    public static void initCreateArchive(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button createArchive)
    {
        createArchive.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = createArchive;
            createArchive.setDisable(true);

            initRoot(stage, anchorPane, detailArea);
        });
    }

    public static void initAddFile(Stage stage, TextArea detailArea, Button addFile, Button create, PasswordField password, PasswordField passwordCheck)
    {
        addFile.setOnAction(event ->
        {
            try
            {
                List<FileChooser.ExtensionFilter> extensionFilter = Collections.singletonList(new FileChooser.ExtensionFilter("All Files", "*.*"));

                FileChooser fileChooser = creator.createFileChooser("Choose some files to archive", null, null, extensionFilter);

                List<File> files = fileChooser.showOpenMultipleDialog(stage);

                if (files == null)
                {
                    if (chosenFiles.isEmpty() && chosenDirectories.isEmpty())
                    {
                        create.setDisable(true);
                    }
                    return;
                }

                for (File file : files)
                {
                    if (!chosenFiles.contains(file))
                    {
                        validAction(detailArea, "Added file '" + file.getAbsolutePath() + "' to archive.");
                    }
                }

                chosenFiles.addAll(files);

                initCreationPreview(detailArea, create);

                if (!password.getText().isBlank() && !passwordCheck.getText().isBlank() && passwordCheck.getText().equals(password.getText()))
                {
                    create.setDisable(false);
                }
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));

                if (SettingHandler.pushNotifications)
                {
                    TrayIconHandler.sendErrorPushNotification(detailArea, e);
                }
            }
        });
    }

    public static void initAddFolder(Stage stage, TextArea detailArea, Button addFolder, Button create, PasswordField password, PasswordField passwordCheck)
    {
        addFolder.setOnAction(event ->
        {
            try
            {
                DirectoryChooser directoryChooser = creator.createDirectoryChooser("Choose a directory to archive");

                File directory = directoryChooser.showDialog(stage);

                if (directory == null)
                {
                    if (chosenFiles.isEmpty() && chosenDirectories.isEmpty())
                    {
                        create.setDisable(true);
                    }
                    return;
                }

                if (!chosenDirectories.contains(directory))
                {
                    validAction(detailArea, "Added directory " + directory.getAbsolutePath() + " to archive.");
                }

                chosenDirectories.add(directory);

                initCreationPreview(detailArea, create);

                if (!password.getText().isBlank() && !passwordCheck.getText().isBlank() && passwordCheck.getText().equals(password.getText()))
                {
                    create.setDisable(false);
                }
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));

                if (SettingHandler.pushNotifications)
                {
                    TrayIconHandler.sendErrorPushNotification(detailArea, e);
                }
            }
        });
    }

    public static void initCreate(Stage stage, TextArea detailArea, Button create, PasswordField password, TextField comment)
    {
        create.setOnAction(event ->
        {
            try
            {
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setFileComment(comment.getText());
                zipParameters.setAesKeyStrength(aesks);
                zipParameters.setCompressionLevel(cpl);
                zipParameters.setEncryptionMethod(em);
                zipParameters.setEncryptFiles(true);
                zipParameters.setAesVersion(aesv);

                List<FileChooser.ExtensionFilter> extensionFilter = Collections.singletonList(new FileChooser.ExtensionFilter("VorteX Archive", "*.vxar"));

                FileChooser fileChooser = creator.createFileChooser("Save as...", null, null, extensionFilter);

                File saveToFile = fileChooser.showSaveDialog(stage);

                if (saveToFile == null)
                {
                    return;
                }

                validAction(detailArea, "File '" + saveToFile.getName() + "' will be saved to directory " + saveToFile.getParent());
                validAction(detailArea, "Archiving started...");

                ZipFile zipFile = new ZipFile(saveToFile, password.getText().toCharArray());

                if (!chosenFiles.isEmpty())
                {
                    ArrayList<File> files = new ArrayList<>(chosenFiles);

                    zipFile.addFiles(files, zipParameters);
                }

                if (!chosenDirectories.isEmpty())
                {
                    for (File directory : chosenDirectories)
                    {
                        zipFile.addFolder(directory, zipParameters);
                    }
                }

                Base32Codec.encodeAndRenameFile(zipFile);

                int fileCount = chosenFiles.size();
                int directoryCount = chosenDirectories.size();

                for (File directory : chosenDirectories)
                {
                    Collection<File> allFiles = FileUtils.listFilesAndDirs(directory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);

                    for (File file : allFiles)
                    {
                        if (file.getAbsolutePath().equals(directory.getAbsolutePath()))
                        {
                            continue;
                        }

                        if (file.isDirectory())
                        {
                            directoryCount++;
                            continue;
                        }

                        fileCount++;
                    }
                }

                String text = "Successfully archived " + fileCount + " files and " + directoryCount + " directories to " + saveToFile.getName() + " in '" + saveToFile.getParent() + "'.";

                validAction(detailArea, text);

                create.setDisable(true);

                chosenFiles.clear();
                chosenDirectories.clear();

                if (SettingHandler.pushNotifications)
                {
                    SystemTray tray = SystemTray.getSystemTray();

                    Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                    TrayIcon trayIcon = new TrayIcon(image);
                    trayIcon.setImageAutoSize(true);
                    trayIcon.addActionListener((e) -> WindowsExplorer.openDirectoryAndHighlightFile(detailArea, zipFile.getFile()));

                    tray.add(trayIcon);

                    String caption = "Successfully created '" + zipFile.getFile().getName() + "'";

                    trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);

                    TimeUnit.MILLISECONDS.sleep(500);

                    tray.remove(trayIcon);
                }

                if (SettingHandler.autoOpenExplorer)
                {
                    WindowsExplorer.openDirectoryAndHighlightFile(detailArea, zipFile.getFile());
                }
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));

                if (SettingHandler.pushNotifications)
                {
                    TrayIconHandler.sendErrorPushNotification(detailArea, e);
                }
            }
        });
    }

    public static void initCreationPreview(TextArea detailArea, Button create)
    {
        AnchorPane anchorPane = new AnchorPane();

        CheckBoxTreeItem<File> rootItem = new CheckBoxTreeItem<>();

        ArrayList<CheckBoxTreeItem<File>> checkBoxTreeItems = new ArrayList<>();

        for (File file : chosenFiles)
        {
            CheckBoxTreeItem<File> checkBoxTreeItem = new CheckBoxTreeItem<>(file);
            checkBoxTreeItem.setSelected(true);

            checkBoxTreeItem.selectedProperty().addListener((obs, oldVal, newVal) ->
            {
                if (!newVal)
                {
                    chosenFiles.remove(file);

                    if (chosenFiles.isEmpty() && chosenDirectories.isEmpty())
                    {
                        create.setDisable(true);
                    }

                    validAction(detailArea, "Removed file '" + file.getAbsolutePath() + "' from archive.");
                    return;
                }

                chosenFiles.add(file);

                validAction(detailArea, "Added file '" + file.getAbsolutePath() + "' to archive.");

                create.setDisable(false);
            });

            checkBoxTreeItems.add(checkBoxTreeItem);
        }

        for (File directory : chosenDirectories)
        {
            CheckBoxTreeItem<File> checkBoxTreeItem = new CheckBoxTreeItem<>(directory);
            checkBoxTreeItem.setSelected(true);

            checkBoxTreeItem.selectedProperty().addListener((obs, oldVal, newVal) ->
            {
                if (!newVal)
                {
                    chosenDirectories.remove(directory);

                    if (chosenFiles.isEmpty() && chosenDirectories.isEmpty())
                    {
                        create.setDisable(true);
                    }

                    validAction(detailArea, "Removed directory '" + directory.getAbsolutePath() + "' from archive.");
                    return;
                }

                chosenDirectories.add(directory);

                validAction(detailArea, "Added directory '" + directory.getAbsolutePath() + "' to archive.");

                create.setDisable(false);
            });

            checkBoxTreeItems.add(checkBoxTreeItem);
        }

        rootItem.getChildren().addAll(checkBoxTreeItems);

        CheckTreeView<File> checkTreeView = new CheckTreeView<>(rootItem);
        checkTreeView.setShowRoot(false);
        checkTreeView.setPrefWidth(400);

        checkTreeView.setCellFactory(tv -> new CheckBoxTreeCell<>()
        {
            @Override
            public void updateItem(File file, boolean empty)
            {
                super.updateItem(file, empty);

                setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);

                if (!empty || file != null)
                {
                    setText(file.getName());
                }
            }
        });

        checkTreeView.setOnMouseClicked(event ->
        {
            if (event.getClickCount() == 2)
            {
                WindowsExplorer.openDirectoryAndHighlightFile(detailArea, checkTreeView.getSelectionModel().getSelectedItem().getValue());
            }
        });

        anchorPane.getChildren().add(checkTreeView);

        creationPreview = creator.createPopOver(anchorPane);

        create.setOnMouseEntered(event ->
        {
            if (!creationPreview.isShowing())
            {
                creationPreview.show(create);
            }
        });

        AtomicReference<Point2D> dragStartLocation = new AtomicReference<>();

        create.setOnMouseExited(event ->
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
                    creationPreview.hide();
                    return;
                }

                if (dragStartLocation.get() == null && creationPreview.isShowing())
                {
                    creationPreview.hide();
                }
            });

            finalScrollbarV.lookup(".thumb").setOnMousePressed(em -> dragStartLocation.set(new Point2D(em.getScreenX(), em.getScreenY())));
            finalScrollbarV.lookup(".thumb").setOnMouseReleased(emr -> dragStartLocation.set(null));

            finalScrollbarH.lookup(".thumb").setOnMousePressed(em -> dragStartLocation.set(new Point2D(em.getScreenX(), em.getScreenY())));
            finalScrollbarH.lookup(".thumb").setOnMouseReleased(emr -> dragStartLocation.set(null));
        });
    }

    public static void initShowPassword(CheckBox showPassword, PasswordField password, TextField passwordUnmasked)
    {
        showPassword.setOnAction(event ->
        {
            passwordUnmasked.managedProperty().bind(showPassword.selectedProperty());
            passwordUnmasked.visibleProperty().bind(showPassword.selectedProperty());

            password.managedProperty().bind(showPassword.selectedProperty().not());
            password.visibleProperty().bind(showPassword.selectedProperty().not());

            passwordUnmasked.textProperty().bindBidirectional(password.textProperty());
        });
    }

    public static void initShowPasswordCheck(CheckBox showPasswordCheck, PasswordField passwordCheck, TextField passwordCheckUnmasked)
    {
        showPasswordCheck.setOnAction(event ->
        {
            passwordCheckUnmasked.managedProperty().bind(showPasswordCheck.selectedProperty());
            passwordCheckUnmasked.visibleProperty().bind(showPasswordCheck.selectedProperty());

            passwordCheck.managedProperty().bind(showPasswordCheck.selectedProperty().not());
            passwordCheck.visibleProperty().bind(showPasswordCheck.selectedProperty().not());

            passwordCheckUnmasked.textProperty().bindBidirectional(passwordCheck.textProperty());
        });
    }

    public static void initCompressionLevel(ComboBox<CompressionLevel> compressionLevel)
    {
        compressionLevel.setOnAction(e -> cpl = compressionLevel.getValue());
    }

    public static void initEncryptionMethod(ComboBox<EncryptionMethod> encryptionMethod, ComboBox<AesKeyStrength> keyStrength, RadioButton aes1, RadioButton aes2)
    {
        encryptionMethod.setOnAction(event ->
        {
            em = encryptionMethod.getValue();

            switch (em)
            {
                case ZIP_STANDARD ->
                {
                    aes1.setVisible(false);
                    aes2.setVisible(false);
                    keyStrength.setVisible(false);

                    encryptionMethod.setPrefWidth(160);
                }
                case ZIP_STANDARD_VARIANT_STRONG ->
                {
                    aes1.setVisible(false);
                    aes2.setVisible(false);
                    keyStrength.setVisible(false);

                    encryptionMethod.setPrefWidth(300);
                }
                case AES ->
                {
                    aes1.setVisible(true);
                    aes2.setVisible(true);
                    keyStrength.setVisible(true);

                    encryptionMethod.setPrefWidth(80);
                }
            }
        });
    }

    public static void initAESVersion(ToggleGroup aesVersionGroup, RadioButton aes1)
    {
        aesVersionGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) ->
        {
            if (newValue == aes1)
            {
                aesv = AesVersion.ONE;
                return;
            }
            aesv = AesVersion.TWO;
        });
    }

    public static void initAESKeyStrength(ComboBox<AesKeyStrength> aesKeyStrength)
    {
        aesKeyStrength.setOnAction(event -> aesks = aesKeyStrength.getValue());
    }

    public static void initClear(TextArea detailArea, Button clear, Button create, PasswordField password, PasswordField passwordCheck,
                                 CheckBox showPassword, CheckBox showPasswordCheck, ComboBox<CompressionLevel> compressionLevel, TextField comment,
                                 ComboBox<EncryptionMethod> encryptionMethod, RadioButton aes1, RadioButton aes2, ComboBox<AesKeyStrength> aesKeyStrength)
    {
        clear.setOnAction(event ->
        {
            detailArea.clear();

            create.setDisable(true);

            password.clear();
            passwordCheck.clear();

            showPassword.setSelected(false);
            showPasswordCheck.setSelected(false);

            compressionLevel.getSelectionModel().clearAndSelect(5);

            comment.clear();

            encryptionMethod.getSelectionModel().clearAndSelect(2);

            aes1.setSelected(false);
            aes2.setSelected(true);

            aesKeyStrength.getSelectionModel().clearAndSelect(2);

            chosenFiles.clear();
            chosenDirectories.clear();
        });
    }
}
