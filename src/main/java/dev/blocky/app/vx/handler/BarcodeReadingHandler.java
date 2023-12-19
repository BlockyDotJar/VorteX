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

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.*;
import com.google.zxing.oned.rss.RSS14Reader;
import com.google.zxing.oned.rss.expanded.RSSExpandedReader;
import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.windows.api.WindowsExplorer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static dev.blocky.app.vx.handler.ActionHandler.*;
import static dev.blocky.app.vx.handler.BarcodeCreationHandler.initBarcodeFormat;
import static dev.blocky.app.vx.handler.TrayIconHandler.sendErrorPushNotification;
import static dev.blocky.app.vx.handler.TrayIconHandler.sendPushNotification;

public class BarcodeReadingHandler
{
    private static final NodeCreator creator = new NodeCreator();

    private static BarcodeFormat barcodeFormatValue = BarcodeFormat.CODE_128;
    private static ImageView bcImage;

    public static void initReadBarcode(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button readBarcode)
    {
        readBarcode.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = readBarcode;
            readBarcode.setDisable(true);

            anchorPane.getChildren().clear();

            TextField barcodeOutput = creator.createTextField("Choose a barcode image to get the output", null, 10, 300, 585, true, true, false, false);

            Button chooseBarcodeImage = creator.createButton("Choose barcode image", 10, 250, 200, false);
            Button clear = creator.createButton("Clear", 500, 250, 90, false);

            ObservableList<BarcodeFormat> barcodeFormats = FXCollections.observableArrayList
                    (
                            BarcodeFormat.CODABAR,
                            BarcodeFormat.CODE_39, BarcodeFormat.CODE_93, BarcodeFormat.CODE_128,
                            BarcodeFormat.EAN_8, BarcodeFormat.EAN_13,
                            BarcodeFormat.ITF,
                            BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                            BarcodeFormat.RSS_14, BarcodeFormat.RSS_EXPANDED
                    );

            ComboBox<BarcodeFormat> barcodeFormat = creator.createComboBox("CODE_128", 245, 252, 220, barcodeFormats, false);

            anchorPane.getChildren().addAll(detailArea, barcodeOutput, chooseBarcodeImage, clear, barcodeFormat);

            initChooseBarcodeImage(stage, anchorPane, detailArea, chooseBarcodeImage, barcodeOutput);
            initBarcodeFormat(barcodeFormat);
            initClear(anchorPane, detailArea, clear, barcodeFormat, barcodeOutput);
        });
    }

    public static void initChooseBarcodeImage(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button chooseBarcodeFile, TextField barcodeOutput)
    {
        chooseBarcodeFile.setOnAction(event ->
        {
            try
            {
                List<FileChooser.ExtensionFilter> extensionFilter = List.of
                        (
                                new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg"),
                                new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                                new FileChooser.ExtensionFilter("JPEG Image", "*.jpg")
                        );

                String initialDirectory = System.getProperty("user.home") + "\\Pictures";

                FileChooser fileChooser = creator.createFileChooser("Choose barcode image file", initialDirectory, null, extensionFilter);

                File file = fileChooser.showOpenDialog(stage);

                anchorPane.getChildren().remove(bcImage);

                if (file == null)
                {
                    return;
                }

                Reader odcr = null;

                switch (barcodeFormatValue)
                {
                    case CODABAR -> odcr = new CodaBarReader();
                    case CODE_39 -> odcr = new Code39Reader();
                    case CODE_93 -> odcr = new Code93Reader();
                    case CODE_128 -> odcr = new Code128Reader();
                    case EAN_8 -> odcr = new EAN8Reader();
                    case EAN_13 -> odcr = new EAN13Reader();
                    case ITF -> odcr = new ITFReader();
                    case UPC_A -> odcr = new UPCAReader();
                    case UPC_E -> odcr = new UPCEReader();
                    case RSS_14 -> odcr = new RSS14Reader();
                    case RSS_EXPANDED -> odcr = new RSSExpandedReader();
                }

                if (!Files.isReadable(file.toPath()))
                {
                    String caption = "Error while trying to read a file.";
                    String text = "You don't have permission to read the content of " + file.getName() + ". Try running VorteX with administrator privileges or reinstall VorteX at %APPDATA%.";

                    sendPushNotification(detailArea, null, TrayIcon.MessageType.ERROR, caption, text);
                    return;
                }

                BufferedImage barcode = ImageIO.read(file);
                Image barcodeImage = SwingFXUtils.toFXImage(barcode, null);

                int[] pixels = barcode.getRGB(0, 0, barcode.getWidth(), barcode.getHeight(), null, 0, barcode.getWidth());

                RGBLuminanceSource source = new RGBLuminanceSource(barcode.getWidth(), barcode.getHeight(), pixels);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

                Result result = odcr.decode(binaryBitmap, null);

                ImageView imageView = creator.createImageView(barcodeImage, 300, 150, 150, 55);

                initOpenBarcode(detailArea, imageView, file);

                barcodeOutput.setText(result.getText());

                anchorPane.getChildren().add(imageView);

                bcImage = imageView;

                validAction(detailArea, "Successfully scanned " + barcodeFormatValue + " barcode.");
            }
            catch (Exception e)
            {
                sendErrorPushNotification(detailArea, e);

                if (e instanceof NotFoundException)
                {
                    invalidAction(detailArea, "No barcode found in image, invalid barcode provided or wrong barcode format selected.");
                    return;
                }

                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public static void initOpenBarcode(TextArea detailArea, ImageView imageView, File file)
    {
        imageView.setOnMouseClicked(event ->
        {
            if (event.getClickCount() == 2)
            {
                WindowsExplorer.openDirectoryAndHighlightFile(detailArea, file);
            }
        });
    }

    public static void initClear(AnchorPane anchorPane, TextArea detailArea, Button clear, ComboBox<BarcodeFormat> barcodeFormat, TextField barcodeOutput)
    {
        clear.setOnAction(event ->
        {
            detailArea.clear();

            barcodeFormat.getSelectionModel().clearAndSelect(3);

            barcodeOutput.clear();

            anchorPane.getChildren().remove(bcImage);

            barcodeFormatValue = BarcodeFormat.CODE_128;
        });
    }
}
