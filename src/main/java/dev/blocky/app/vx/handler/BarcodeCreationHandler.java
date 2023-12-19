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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.*;
import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.windows.api.WindowsExplorer;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.blocky.app.vx.handler.ActionHandler.*;
import static dev.blocky.app.vx.handler.SettingHandler.autoOpenExplorer;
import static dev.blocky.app.vx.handler.TrayIconHandler.sendErrorPushNotification;
import static dev.blocky.app.vx.handler.TrayIconHandler.sendPushNotification;

public class BarcodeCreationHandler
{
    private static final NodeCreator creator = new NodeCreator();

    private static BarcodeFormat barcodeFormatValue = BarcodeFormat.CODE_128;
    private static ImageView bcImage;

    public static void initCreateBarcode(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button createBarcode)
    {
        createBarcode.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = createBarcode;
            createBarcode.setDisable(true);

            anchorPane.getChildren().clear();

            Button generateBarcode = creator.createButton("Generate", 10, 250, 100, true);
            Button clear = creator.createButton("Clear", 500, 250, 90, false);

            TextField barcodeInput = creator.createTextField("Enter the input for the barcode", null, 10, 300, 380, true, true, true, false);

            barcodeInput.textProperty().addListener((obs, oldVal, newVal) ->
            {
                if (newVal.isBlank())
                {
                    generateBarcode.setDisable(true);
                    return;
                }

                generateBarcode.setDisable(false);
            });

            TextField width = creator.createTextField("Width", null, 400, 300, 90, true, true, true, false);
            width.textProperty().addListener(createTextFieldChangeListener(width));

            TextField height = creator.createTextField("Height", null, 500, 300, 90, true, true, true, false);
            height.textProperty().addListener(createTextFieldChangeListener(height));

            ObservableList<BarcodeFormat> barcodeFormats = FXCollections.observableArrayList
                    (
                            BarcodeFormat.CODABAR,
                            BarcodeFormat.CODE_39, BarcodeFormat.CODE_93, BarcodeFormat.CODE_128,
                            BarcodeFormat.EAN_8, BarcodeFormat.EAN_13,
                            BarcodeFormat.ITF,
                            BarcodeFormat.UPC_A, BarcodeFormat.UPC_E
                    );

            ComboBox<BarcodeFormat> barcodeFormat = creator.createComboBox("CODE_128", 170, 252, 220, barcodeFormats, false);

            anchorPane.getChildren().addAll(detailArea, barcodeInput, width, height, generateBarcode, clear, barcodeFormat);

            initGenerateBarcode(stage, anchorPane, detailArea, generateBarcode, barcodeInput, width, height);
            initBarcodeFormat(barcodeFormat);
            initClear(anchorPane, detailArea, clear, generateBarcode, barcodeFormat, barcodeInput, width, height);
        });
    }


    public static void initGenerateBarcode(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button generateBarcode, TextField barcodeInput,
                                           TextField width, TextField height)
    {
        generateBarcode.setOnAction(event ->
        {
            try
            {
                String barcodeInputText = barcodeInput.getText();
                String widthText = width.getText();
                String heightText = height.getText();

                int iWidth = widthText == null || widthText.isBlank() ? 300 : Integer.parseInt(widthText);

                if (iWidth == 0)
                {
                    invalidAction(detailArea, "Width can't be 0.");
                    return;
                }

                int iHeight = heightText == null || heightText.isBlank() ? 150 : Integer.parseInt(heightText);

                if (iHeight == 0)
                {
                    invalidAction(detailArea, "Height can't be 0.");
                    return;
                }

                Writer odcw = null;
                Pattern BARCODE_LEGAL_CHARACTER_PATTERN = null;

                switch (barcodeFormatValue)
                {
                    case CODABAR ->
                    {
                        odcw = new CodaBarWriter();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^[\\d$:/.+-]+$");
                    }
                    case CODE_39 ->
                    {
                        odcw = new Code39Writer();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^[A-Z\\d\u0020.$/+%-]+$");
                    }
                    case CODE_93 ->
                    {
                        odcw = new Code93Writer();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^[A-Z\\d\u0020.$/+%-]+$");
                    }
                    case CODE_128 ->
                    {
                        odcw = new Code128Writer();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^[\\w\u0020!\"#$%&'()*+,./:;<=>?@\\[\\]\\\\^`{|}-]+$");
                    }
                    case EAN_8 ->
                    {
                        odcw = new EAN8Writer();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^\\d{7,8}$");
                    }
                    case EAN_13 ->
                    {
                        odcw = new EAN13Writer();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^\\d{12,13}$");
                    }
                    case ITF ->
                    {
                        odcw = new ITFWriter();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^\\d{14}$");
                    }
                    case UPC_A ->
                    {
                        odcw = new UPCAWriter();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^\\d{11,12}$");
                    }
                    case UPC_E ->
                    {
                        odcw = new UPCEWriter();
                        BARCODE_LEGAL_CHARACTER_PATTERN = Pattern.compile("^\\d{7,8}$");
                    }
                }

                Matcher BARCODE_LEGAL_CHARACTER_MATCHER = BARCODE_LEGAL_CHARACTER_PATTERN.matcher(barcodeInputText);

                if (!BARCODE_LEGAL_CHARACTER_MATCHER.matches())
                {
                    invalidAction(detailArea, "Barcode input doesn't match with RegEx Pattern " + BARCODE_LEGAL_CHARACTER_PATTERN);
                    return;
                }

                BitMatrix bitMatrix = odcw.encode(barcodeInputText, barcodeFormatValue, 300, 150);

                BufferedImage barcode = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig());
                Image barcodeImage = SwingFXUtils.toFXImage(barcode, null);

                ImageView imageView = creator.createImageView(barcodeImage, 300, 150, 150, 55);

                ContextMenu contextMenu = new ContextMenu();
                MenuItem saveImage = new MenuItem("Save Image");
                MenuItem copyImage = new MenuItem("Copy Image");

                contextMenu.getItems().addAll(saveImage, copyImage);
                anchorPane.getChildren().remove(bcImage);

                initSaveBarcode(stage, detailArea, width, height, saveImage, barcode, imageView);
                initCopyBarcode(detailArea, width, height, copyImage, barcode, imageView);
                imageView.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));

                anchorPane.getChildren().add(imageView);

                bcImage = imageView;

                String text = String.format("Successfully created %s barcode with value '%s', a width of %d and a height of %d.", barcodeFormatValue, barcodeInputText, iWidth, iHeight);

                validAction(detailArea, text);
            }
            catch (Exception e)
            {
                sendErrorPushNotification(detailArea, e);

                if (e instanceof IllegalArgumentException)
                {
                    invalidAction(detailArea, e.getMessage() + ".");
                    return;
                }

                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public static void initSaveBarcode(Stage stage, TextArea detailArea, TextField width, TextField height, MenuItem saveImage,
                                       BufferedImage barcode, ImageView imageView)
    {
        saveImage.setOnAction(event ->
                {
                    try
                    {
                        List<FileChooser.ExtensionFilter> extensionFilters = List.of
                                (
                                        new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg"),
                                        new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                                        new FileChooser.ExtensionFilter("JPEG Image", "*.jpg")
                                );

                        String initialDirectory = System.getProperty("user.home") + "\\Pictures";

                        FileChooser fileChooser = creator.createFileChooser("Save as...", initialDirectory, "barcode.png", extensionFilters);
                        File file = fileChooser.showSaveDialog(stage);

                        if (file == null)
                        {
                            return;
                        }

                        if (!Files.isWritable(file.getParentFile().toPath()))
                        {
                            String caption = "Error while trying to write to file.";
                            String text = "You don't have permission to write to " + file.getParentFile().getName() + ". Try saving the file at a subdirectory of your user directory or run VorteX with administrator privileges.";

                            sendPushNotification(detailArea, null, TrayIcon.MessageType.ERROR, caption, text);
                            return;
                        }

                        validAction(detailArea, "File '" + file.getName() + "' will be created in directory " + file.getParent());
                        validAction(detailArea, "Creation started...");

                        String widthText = width.getText();
                        String heightText = height.getText();

                        int iWidth = widthText == null || widthText.isBlank() ? 300 : Integer.parseInt(widthText);
                        int iHeight = widthText == null || heightText.isBlank() ? 150 : Integer.parseInt(heightText);

                        Image resizedBarcodeImage = imageView.getImage();

                        if ((widthText != null && !widthText.isBlank()) || (heightText != null && !heightText.isBlank()))
                        {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            ImageIO.write(barcode, FilenameUtils.getExtension(file.getName()), outputStream);

                            byte[] res = outputStream.toByteArray();

                            ByteArrayInputStream inputStream = new ByteArrayInputStream(res);

                            resizedBarcodeImage = new Image(inputStream, iWidth, iHeight, false, false);
                        }

                        /*
                            Very important. Needed for jpg files to be created with the ImageIO#write method.
                            See https://stackoverflow.com/a/57674578
                         */

                        BufferedImage barcodeWithoutAlpha = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);

                        ImageIO.write(SwingFXUtils.fromFXImage(resizedBarcodeImage, barcodeWithoutAlpha), FilenameUtils.getExtension(file.getName()), file);

                        String text = "Successfully created image '" + file.getName() + "' in directory " + file.getParent();

                        validAction(detailArea, text);

                        String caption = "Successfully created barcode '" + file.getName() + "'";

                        sendPushNotification(detailArea, (e) -> WindowsExplorer.openDirectoryAndHighlightFile(detailArea, file), TrayIcon.MessageType.INFO, caption, text);

                        if (autoOpenExplorer)
                        {
                            WindowsExplorer.openDirectoryAndHighlightFile(detailArea, file);
                        }
                    }
                    catch (Exception e)
                    {
                        invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
                    }
                }
        );
    }

    public static void initCopyBarcode(TextArea detailArea, TextField width, TextField height, MenuItem copyImage, BufferedImage barcode, ImageView imageView)
    {
        copyImage.setOnAction(event ->
                {
                    try
                    {
                        String widthText = width.getText();
                        String heightText = height.getText();

                        int iWidth = widthText == null || widthText.isBlank() ? 300 : Integer.parseInt(widthText);
                        int iHeight = heightText == null || heightText.isBlank() ? 150 : Integer.parseInt(heightText);

                        Image resizedBarcodeImage = imageView.getImage();

                        if ((widthText != null && !widthText.isBlank()) || (heightText != null && !heightText.isBlank()))
                        {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            ImageIO.write(barcode, "jpg", outputStream);

                            byte[] res = outputStream.toByteArray();

                            ByteArrayInputStream inputStream = new ByteArrayInputStream(res);

                            resizedBarcodeImage = new Image(inputStream, iWidth, iHeight, false, false);
                        }

                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();

                        content.putImage(resizedBarcodeImage);

                        clipboard.setContent(content);

                        validAction(detailArea, "Successfully copied barcode image to clipboard.");
                    }
                    catch (Exception e)
                    {
                        invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
                        sendErrorPushNotification(detailArea, e);
                    }
                }
        );
    }

    public static void initBarcodeFormat(ComboBox<BarcodeFormat> barcodeFormat)
    {
        barcodeFormat.setOnAction(event -> barcodeFormatValue = barcodeFormat.getValue());
    }

    public static void initClear(AnchorPane anchorPane, TextArea detailArea, Button clear, Button generateBarcode, ComboBox<BarcodeFormat> barcodeFormat,
                                 TextField barcodeInput, TextField width, TextField height)
    {
        clear.setOnAction(event ->
        {
            detailArea.clear();

            generateBarcode.setDisable(true);

            barcodeFormat.getSelectionModel().clearAndSelect(3);

            barcodeInput.clear();
            width.clear();
            height.clear();

            anchorPane.getChildren().remove(bcImage);
        });
    }

    private static ChangeListener<? super String> createTextFieldChangeListener(TextField textField)
    {
        return (obs, oldVal, newVal) ->
        {
            if (!newVal.matches("\\d*"))
            {
                textField.setText(RegExUtils.removeAll(newVal, "[^\\d.]"));
            }
        };
    }
}
