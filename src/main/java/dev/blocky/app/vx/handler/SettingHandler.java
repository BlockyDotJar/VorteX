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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

            Label dwmLabel = creator.createLabel("Choose your mica material style (Windows 11 22H2 and above only)", 10, 10);

            ObservableList<DWMAttribute> dwmAttributes = FXCollections.observableArrayList
                    (
                            DWMAttribute.DWMSBT_DISABLE,
                            DWMAttribute.DWMSBT_MAINWINDOW,
                            DWMAttribute.DWMSBT_TABBEDWINDOW,
                            DWMAttribute.DWMSBT_TRANSIENTWINDOW
                    );

            String displayVersion = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", "DisplayVersion");

            int dvNumber = Integer.parseInt(StringUtils.remove(displayVersion, "H"));
            boolean validVersion = dvNumber >= 222;

            ComboBox<DWMAttribute> dwmAttribute = creator.createComboBox("DWMSBT_MAINWINDOW", 10, 40, 220, dwmAttributes, !validVersion);

            Label darkModeLabel = creator.createLabel("User immersive dark mode", 60, 90);

            ToggleSwitch darkModeSwitch = creator.createToggleSwitch(-8, 90, !validVersion);

            anchorPane.getChildren().addAll(detailArea, dwmLabel, dwmAttribute, darkModeLabel, darkModeSwitch);
        });
    }
}
