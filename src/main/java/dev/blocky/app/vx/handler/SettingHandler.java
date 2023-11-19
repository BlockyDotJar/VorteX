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

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import dev.blocky.app.vx.entities.NodeCreator;
import dev.blocky.app.vx.windows.api.dwm.DWMAttribute;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.ToggleSwitch;

import static dev.blocky.app.vx.handler.ActionHandler.lastUsedButton;

public class SettingHandler
{
    private static final NodeCreator creator = new NodeCreator();

    public static void initSettings(AnchorPane anchorPane, TextArea detailArea, Button settings)
    {
        settings.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = settings;
            settings.setDisable(true);

            anchorPane.getChildren().clear();

            Label dwmLabel = creator.createLabel("Choose your mica material style", 10, 10);

            ObservableList<DWMAttribute> dwmAttributes = FXCollections.observableArrayList
                    (
                            DWMAttribute.DWMSBT_DISABLE,
                            DWMAttribute.DWMSBT_MAINWINDOW,
                            DWMAttribute.DWMSBT_TABBEDWINDOW
                    );

            String displayVersion = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", "DisplayVersion");

            int dvNumber = Integer.parseInt(StringUtils.remove(displayVersion, "H"));
            boolean validVersion = dvNumber >= 222;

            ComboBox<DWMAttribute> dwmAttribute = creator.createComboBox("DWMSBT_MAINWINDOW", 10, 40, 220, dwmAttributes, !validVersion);

            Label immersiveDarkModeLabel = creator.createLabel("Use immersive dark mode", 60, 90);
            ToggleSwitch immersiveDarkModeSwitch = creator.createToggleSwitch(-8, 90, validVersion, !validVersion);

            Label captionLabel = creator.createLabel("Color for titlebar", 10, 120);

            TextField rCaption = creator.createTextField("r", 10, 150, 50, true, true, validVersion, !validVersion);
            TextField gCaption = creator.createTextField("g", 80, 150, 50, true, true, validVersion, !validVersion);
            TextField bCaption = creator.createTextField("b", 150, 150, 50, true, true, validVersion, !validVersion);

            rCaption.textProperty().addListener(createChangeListener(rCaption));
            gCaption.textProperty().addListener(createChangeListener(gCaption));
            bCaption.textProperty().addListener(createChangeListener(bCaption));

            Label textLabel = creator.createLabel("Color for titlebar text", 250, 120);

            TextField rText = creator.createTextField("r", 250, 150, 50, true, true, validVersion, !validVersion);
            TextField gText = creator.createTextField("g", 320, 150, 50, true, true, validVersion, !validVersion);
            TextField bText = creator.createTextField("b", 390, 150, 50, true, true, validVersion, !validVersion);

            rText.textProperty().addListener(createChangeListener(rText));
            gText.textProperty().addListener(createChangeListener(gText));
            bText.textProperty().addListener(createChangeListener(bText));

            Label borderLabel = creator.createLabel("Color for window border", 10, 200);

            TextField rBorder = creator.createTextField("r", 10, 230, 50, true, true, validVersion, !validVersion);
            TextField gBorder = creator.createTextField("g", 80, 230, 50, true, true, validVersion, !validVersion);
            TextField bBorder = creator.createTextField("b", 150, 230, 50, true, true, validVersion, !validVersion);

            rBorder.textProperty().addListener(createChangeListener(rBorder));
            gBorder.textProperty().addListener(createChangeListener(gBorder));
            bBorder.textProperty().addListener(createChangeListener(bBorder));

            // TODO: replace validVersion with the json value or default to it

            Label darkModeLabel = creator.createLabel("Use dark mode (mica must be disabled)", 60, 280);
            ToggleSwitch darkModeSwitch = creator.createToggleSwitch(-8, 280, !validVersion, validVersion);

            Label checkUpdateLabel = creator.createLabel("Automatically check for updates", 60, 305);
            ToggleSwitch checkUpdateSwitch = creator.createToggleSwitch(-8, 305, true, false);

            Label explorerLabel = creator.createLabel("Open Explorer on file creation success", 60, 330);
            ToggleSwitch explorerSwitch = creator.createToggleSwitch(-8, 330, false, false);

            Label pushNotificationLabel = creator.createLabel("Get push notification for executed tasks", 60, 355);
            ToggleSwitch pushNotificationSwitch = creator.createToggleSwitch(-8, 355, true, false);

            String windowsVersion = "You are currently on: " + System.getProperty("os.name") + " " + displayVersion;

            // TODO: Replace this with a hyperlink to https://github.com/BlockyDotJar/VorteX#why-cant-i-change-some-style-settings if displayVersion is not greater or equal than 222

            Label displayVersionLabel = creator.createLabel(windowsVersion, 10, 387);

            Button checkForUpdates = creator.createButton("Check for updates", 445, 375, 150, false);

            anchorPane.getChildren().addAll(detailArea, dwmLabel, dwmAttribute, immersiveDarkModeLabel, immersiveDarkModeSwitch, captionLabel, rCaption, gCaption, bCaption, textLabel, rText, gText, bText, borderLabel, rBorder, gBorder, bBorder, darkModeSwitch, darkModeLabel, checkUpdateSwitch, checkUpdateLabel, explorerSwitch, explorerLabel, pushNotificationSwitch, pushNotificationLabel, displayVersionLabel, checkForUpdates);
        });
    }

    private static ChangeListener<? super String> createChangeListener(TextField textField)
    {
        return (obs, oldVal, newVal) ->
        {
            if (!newVal.matches("\\d*"))
            {
                textField.setText(newVal.replaceAll("[^\\d.]", ""));
            }

            if (newVal.length() > 3)
            {
                textField.setText(newVal.substring(0, 3));
            }

            int i = Integer.parseInt(newVal.length() > 3 ? newVal.substring(0, 3) : newVal);

            if (i > 255)
            {
                textField.setText("255");
            }
        };
    }
}
