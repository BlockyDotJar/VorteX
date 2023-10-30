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
import com.documents4j.conversion.msoffice.MicrosoftPowerpointBridge;
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

public class DocumentConversionHandler
{
    private static final NodeCreator creator = new NodeCreator();

    private static File document;
    private static ImageView docImage;

    public static void initConvertDocument(Stage stage, HostServices hostServices, AnchorPane anchorPane, TextArea detailArea, Button convertDocument)
    {
        convertDocument.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = convertDocument;
            convertDocument.setDisable(true);

            anchorPane.getChildren().clear();

            Button openDocument = creator.createButton("Open Document", 150, 250, 150, false);
            Button convert = creator.createButton("Convert", 310, 250, 125, true);

            anchorPane.getChildren().addAll(detailArea, openDocument, convert);

            initOpenDocument(stage, hostServices, anchorPane, detailArea, openDocument, convert);
            initConvert(stage, anchorPane, detailArea, convert);
        });
    }

    public static void initOpenDocument(Stage stage, HostServices hostServices, AnchorPane anchorPane, TextArea detailArea, Button openDocument, Button convert)
    {
        openDocument.setOnAction(event ->
        {
            try
            {
                List<FileChooser.ExtensionFilter> extensionFilters = List.of
                        (
                                new FileChooser.ExtensionFilter("All Files", "*.docx", "*.docm", "*.doc", "*.dotx", "*.dotm", "*.dot", "*.pptx", "*.pptm", "*.ppt", "*.potx", "*.potm", "*.pot", "*.ppsx", "*.ppsm", "*.pps"),
                                new FileChooser.ExtensionFilter("All Word Documents", "*.docx", "*.docm", "*.doc", "*.dotx", "*.dotm", "*.dot"),
                                new FileChooser.ExtensionFilter("All PowerPoint Presentations", "*.pptx", "*.pptm", "*.ppt", "*.potx", "*.potm", "*.pot", "*.ppsx", "*.ppsm", "*.pps"),
                                new FileChooser.ExtensionFilter("Word Document", "*.docx"),
                                new FileChooser.ExtensionFilter("Word Macro-Enabled Document", "*.docm"),
                                new FileChooser.ExtensionFilter("Word 97-2003 Document", "*.doc"),
                                new FileChooser.ExtensionFilter("Word Template", "*.dotx"),
                                new FileChooser.ExtensionFilter("Word Macro-Enabled Template", "*.dotm"),
                                new FileChooser.ExtensionFilter("Word 97-2003 Template", "*.dot"),
                                new FileChooser.ExtensionFilter("PowerPoint Presentation", "*.pptx"),
                                new FileChooser.ExtensionFilter("PowerPoint Macro-Enabled Presentation", "*.pptm"),
                                new FileChooser.ExtensionFilter("PowerPoint 97-2003 Presentation", "*.ppt"),
                                new FileChooser.ExtensionFilter("PowerPoint Template", "*.potx"),
                                new FileChooser.ExtensionFilter("PowerPoint Macro-Enabled Template", "*.potm"),
                                new FileChooser.ExtensionFilter("PowerPoint 97-2003 Template", "*.pot"),
                                new FileChooser.ExtensionFilter("PowerPoint Show", "*.ppsx"),
                                new FileChooser.ExtensionFilter("PowerPoint Macro-Enabled Show", "*.ppsm"),
                                new FileChooser.ExtensionFilter("PowerPoint 97-2003 Show", "*.pps")
                        );

                String initialDirectory = System.getProperty("user.home") + "\\Documents";
                FileChooser fileChooser = creator.createFileChooser("Choose a file to convert", initialDirectory, null, extensionFilters);

                File file = fileChooser.showOpenDialog(stage);

                if (file == null)
                {
                    if (document == null || !document.exists())
                    {
                        convert.setDisable(true);
                    }
                    return;
                }

                anchorPane.getChildren().remove(docImage);

                String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase();

                ImageView imageView = creator.createImageView(fileExtension, 100, 110, 240, 85);

                initOpenDocument(hostServices, imageView);

                docImage = imageView;

                anchorPane.getChildren().add(imageView);

                convert.setDisable(false);

                validAction(detailArea, "Selected '" + file.getName() + "' for conversion.");

                document = file;
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }

    public static void initOpenDocument(HostServices hostServices, ImageView imageView)
    {
        imageView.setOnMouseClicked(event ->
        {
            if (event.getClickCount() == 2)
            {
                hostServices.showDocument(document.getAbsolutePath());
            }
        });
    }

    public static void initConvert(Stage stage, AnchorPane anchorPane, TextArea detailArea, Button convert)
    {
        convert.setOnAction(event ->
        {
            try
            {
                String fileExtension = FilenameUtils.getExtension(document.getName());

                String initialDirectory = System.getProperty("user.home") + "\\Documents";
                String initialFilename = document.getName().replace(fileExtension, "pdf");
                List<FileChooser.ExtensionFilter> extensionFilter = Collections.singletonList(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

                FileChooser fileChooser = creator.createFileChooser("Save as...", initialDirectory, initialFilename, extensionFilter);

                File saveToFile = fileChooser.showSaveDialog(stage);

                if (saveToFile == null)
                {
                    return;
                }

                validAction(detailArea, "File '" + saveToFile.getName() + "' will be converted in directory " + saveToFile.getParent());
                validAction(detailArea, "Conversion started...");

                LocalConverter.Builder builder = LocalConverter.builder();
                DocumentType as = DocumentType.MS_WORD;

                if (!fileExtension.matches("^do[ct][mx]?$"))
                {
                    as = DocumentType.MS_POWERPOINT;

                    builder.enable(MicrosoftPowerpointBridge.class);
                }

                IConverter converter = builder.build();

                converter.convert(document).as(as)
                        .to(saveToFile).as(DocumentType.PDF)
                        .schedule();

                converter.shutDown();

                anchorPane.getChildren().remove(docImage);

                convert.setDisable(true);

                validAction(detailArea, "Successfully converted '" + document.getName() + "' to '" + saveToFile.getName() + "' in " + saveToFile.getParent());

                document = null;

                WindowsExplorer.openDirectoryAndHighlightFile(detailArea, saveToFile);
            }
            catch (Exception e)
            {
                invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            }
        });
    }
}
