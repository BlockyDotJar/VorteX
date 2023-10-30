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

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.windows.api.WindowsExplorer;
import javafx.application.HostServices;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static dev.blocky.app.vx.handler.ActionHandler.*;

public class PDFConversionHandler
{
    private static final NodeCreator creator = new NodeCreator();

    private static File pdf;
    private static ImageView pdfImage;

    public static void initConvertPDF(Stage stage, HostServices hostServices, AnchorPane anchorPane, TextArea detailArea, Button convertPDF)
    {
        convertPDF.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = convertPDF;
            convertPDF.setDisable(true);

            anchorPane.getChildren().clear();

            Button openPDF = creator.createButton("Open PDF", 150, 250, 115, false);
            Button convert = creator.createButton("Convert", 310, 250, 125, true);

            anchorPane.getChildren().addAll(detailArea, openPDF, convert);

            initOpenPDF(stage, hostServices, anchorPane, detailArea, openPDF, convert);
            initConvert(stage, anchorPane, detailArea, convert);
        });
    }

    public static void initOpenPDF(Stage stage, HostServices hostServices, AnchorPane anchorPane, TextArea detailArea, Button openPDF, Button convert)
    {
        openPDF.setOnAction(event ->
        {
            try
            {
                List<FileChooser.ExtensionFilter> extensionFilters = Collections.singletonList(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

                String initialDirectory = System.getProperty("user.home") + "\\Documents";
                FileChooser fileChooser = creator.createFileChooser("Choose a file to convert", initialDirectory, null, extensionFilters);

                File file = fileChooser.showOpenDialog(stage);

                if (file == null)
                {
                    if (pdf == null || !pdf.exists())
                    {
                        convert.setDisable(true);
                    }
                    return;
                }

                anchorPane.getChildren().remove(pdfImage);

                ImageView imageView = creator.createImageView("pdf", 96, 118, 244, 85);

                initOpenPDF(hostServices, imageView);

                pdfImage = imageView;

                anchorPane.getChildren().add(imageView);

                convert.setDisable(false);

                validAction(detailArea, "Selected '" + file.getName() + "' for conversion.");

                pdf = file;
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public static void initOpenPDF(HostServices hostServices, ImageView imageView)
    {
        imageView.setOnMouseClicked(event ->
        {
            if (event.getClickCount() == 2)
            {
                hostServices.showDocument(pdf.getAbsolutePath());
            }
        });
    }

    public static void initConvert(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button convert)
    {
        convert.setOnAction(event ->
        {
            try
            {
                String initialDirectory = System.getProperty("user.home") + "\\Documents";
                String initialFilename = pdf.getName().replace("pdf", "docx");

                List<FileChooser.ExtensionFilter> extensionFilter = List.of
                        (
                                new FileChooser.ExtensionFilter("All Word Documents", "*.docx", "*.doc"),
                                new FileChooser.ExtensionFilter("Word Document", "*.docx"),
                                new FileChooser.ExtensionFilter("Word 97-2003 Document", "*.doc")
                        );

                FileChooser fileChooser = creator.createFileChooser("Save as...", initialDirectory, initialFilename, extensionFilter);

                File saveToFile = fileChooser.showSaveDialog(stage);

                if (saveToFile == null)
                {
                    return;
                }

                validAction(detailArea, "File '" + saveToFile.getName() + "' will be converted in directory " + saveToFile.getParent());
                validAction(detailArea, "Conversion started...");

                LocalConverter.Builder builder = LocalConverter.builder();
                DocumentType as = DocumentType.DOCX;

                String fileExtension = FilenameUtils.getExtension(saveToFile.getName()).toLowerCase();

                if (fileExtension.equals("doc"))
                {
                    as = DocumentType.DOC;
                }

                IConverter converter = builder.build();

                converter.convert(pdf).as(DocumentType.PDF)
                        .to(saveToFile).as(as)
                        .schedule();

                converter.shutDown();

                anchorPane.getChildren().remove(pdfImage);

                convert.setDisable(true);

                validAction(detailArea, "Successfully converted '" + pdf.getName() + "' to '" + saveToFile.getName() + "' in " + saveToFile.getParent());

                pdf = null;

                WindowsExplorer.openDirectoryAndHighlightFile(detailArea, saveToFile);
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }
}
