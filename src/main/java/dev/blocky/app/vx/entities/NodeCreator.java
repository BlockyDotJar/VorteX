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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.blocky.app.vx.entities;

import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.ToggleSwitch;

import java.io.File;
import java.util.List;

public class NodeCreator
{
    public Button createButton(String text, double x, double y, double prefWidth, boolean disabled)
    {
        Button button = new Button(text);
        button.setPrefSize(prefWidth == -1 ? 100 : prefWidth, 30);
        button.setDisable(disabled);
        button.setTranslateX(x);
        button.setTranslateY(y);
        return button;
    }

    public <T> ComboBox<T> createComboBox(String text, double x, double y, double prefWidth, ObservableList<T> items, boolean disabled)
    {
        ComboBox<T> comboBox = new ComboBox<>(items);
        comboBox.setPrefWidth(prefWidth == -1 ? 195 : prefWidth);
        comboBox.setDisable(disabled);
        comboBox.setPromptText(text);
        comboBox.setTranslateX(x);
        comboBox.setTranslateY(y);
        return comboBox;
    }

    public TextField createTextField(String text, double x, double y, double prefWidth, boolean managed, boolean visible, boolean editable, boolean disabled)
    {
        TextField textField = new TextField();
        textField.setPrefSize(prefWidth == -1 ? 425 : prefWidth, 30);
        textField.setEditable(editable);
        textField.setDisable(disabled);
        textField.setPromptText(text);
        textField.setManaged(managed);
        textField.setVisible(visible);
        textField.setTranslateX(x);
        textField.setTranslateY(y);
        return textField;
    }

    public PasswordField createPasswordField(String text, double x, double y)
    {
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefSize(425, 30);
        passwordField.setPromptText(text);
        passwordField.setTranslateX(x);
        passwordField.setTranslateY(y);
        return passwordField;
    }

    public CheckBox createCheckBox(String text, double x, double y)
    {
        CheckBox checkBox = new CheckBox(text);
        checkBox.setTranslateX(x);
        checkBox.setTranslateY(y);
        return checkBox;
    }

    public TextArea createTextArea(String text, double x, double y)
    {
        TextArea textArea = new TextArea();
        textArea.setPrefSize(585, 85);
        textArea.setEditable(false);
        textArea.setTranslateX(x);
        textArea.setTranslateY(y);
        textArea.setText(text);
        return textArea;
    }

    public FileChooser createFileChooser(String title, String initialDirectory, String initialFilename, List<FileChooser.ExtensionFilter> extensionFilters)
    {
        File userHome = new File(initialDirectory == null ? System.getProperty("user.home") : initialDirectory);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(userHome);
        fileChooser.setInitialFileName(initialFilename);
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(extensionFilters);
        return fileChooser;
    }

    public DirectoryChooser createDirectoryChooser(String title)
    {
        File userHome = new File(System.getProperty("user.home"));

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(userHome);
        directoryChooser.setTitle(title);
        return directoryChooser;
    }

    public ImageView createImageView(Image image, double width, double height, double x, double y)
    {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setTranslateX(x);
        imageView.setTranslateY(y);
        return imageView;
    }

    public RadioButton createRadioButton(String text, ToggleGroup toggleGroup, double x, double y)
    {
        RadioButton radioButton = new RadioButton(text);
        radioButton.setSelected(true);
        radioButton.setToggleGroup(toggleGroup);
        radioButton.setTranslateX(x);
        radioButton.setTranslateY(y);
        return radioButton;
    }

    public PopOver createPopOver(Node node)
    {
        PopOver popOver = new PopOver(node);
        popOver.setAutoHide(true);
        return popOver;
    }

    public Alert createAlert(Alert.AlertType alertType, String title, String headerText, Node content)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.getDialogPane().setContent(content);
        return alert;
    }

    public Alert createAlert(Alert.AlertType alertType, String title, String headerText, String contentText)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        return alert;
    }

    public WebView createWebView(double x, double y)
    {
        WebView webView = new WebView();
        webView.setMinSize(705, 390);
        webView.setMaxSize(705, 390);
        webView.setTranslateX(x);
        webView.setTranslateY(y);
        return webView;
    }

    public Label createLabel(String text, double x, double y)
    {
        Label label = new Label(text);
        label.setTranslateX(x);
        label.setTranslateY(y);
        return label;
    }

    public Hyperlink createHyperlink(HostServices hostServices, String text, String releaseLink, double x, double y)
    {
        Hyperlink hyperlink = new Hyperlink(text);
        hyperlink.setTranslateX(x);
        hyperlink.setTranslateY(y);
        hyperlink.setOnAction((e) -> hostServices.showDocument(releaseLink));
        return hyperlink;
    }

    public ToggleSwitch createToggleSwitch(double x, double y, boolean selected, boolean disabled)
    {
        ToggleSwitch toggleSwitch = new ToggleSwitch();
        toggleSwitch.setDisable(disabled);
        toggleSwitch.setSelected(selected);
        toggleSwitch.setTranslateX(x);
        toggleSwitch.setTranslateY(y);
        return toggleSwitch;
    }
}
