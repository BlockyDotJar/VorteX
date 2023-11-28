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
import dev.blocky.app.vx.windows.api.dwm.DWMHandler;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.control.ToggleSwitch;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static dev.blocky.app.vx.handler.ActionHandler.invalidAction;
import static dev.blocky.app.vx.handler.ActionHandler.lastUsedButton;
import static dev.blocky.app.vx.handler.TrayIconHandler.sendErrorPushNotification;
import static dev.blocky.app.vx.updater.ApplicationUpdater.initApplicationUpdater;
import static dev.blocky.app.vx.windows.api.dwm.DWMHandler.dwmSetIntValue;
import static dev.blocky.app.vx.windows.api.dwm.DWMHandler.setMicaStyle;
import static java.nio.file.Files.writeString;

public class SettingHandler
{
    private static final NodeCreator creator = new NodeCreator();

    private static DWMAttribute dwma = DWMAttribute.DWMSBT_MAINWINDOW;

    private static TextField rCaption, gCaption, bCaption;
    private static TextField rText, gText, bText;
    private static TextField rBorder, gBorder, bBorder;

    public static boolean updateCheck = true, pushNotifications = true, autoOpenExplorer = false;

    public static void initSettings(HostServices hostServices, Scene scene, AnchorPane anchorPane, TextArea detailArea, String script, Button settings)
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

            boolean validVersion = dvNumber >= 222 && SystemUtils.IS_OS_WINDOWS_11;

            JSONObject root = new JSONObject(readSettings(detailArea));

            JSONObject dwm = root.getJSONObject("dwm");

            int windowType = dwm.getInt("window-type");
            boolean immersiveDarkMode = dwm.getBoolean("immersive-dark-mode");

            dwma = DWMAttribute.findAttribute(windowType);

            ComboBox<DWMAttribute> dwmAttribute = creator.createComboBox(dwma.name(), 10, 40, dwma.value == 1 ? 185 : dwma.value == 2 ? 220 : 247, dwmAttributes, !validVersion);

            Label immersiveDarkModeLabel = creator.createLabel("Use immersive dark mode", 60, 90);
            ToggleSwitch immersiveDarkModeSwitch = creator.createToggleSwitch(-8, 90, validVersion && immersiveDarkMode, !validVersion || dwma.value == 1);

            Label captionLabel = creator.createLabel("Color for titlebar", 10, 120);

            JSONObject caption = dwm.getJSONObject("caption");

            int rCaptionInt = caption.getInt("r");
            int gCaptionInt = caption.getInt("g");
            int bCaptionInt = caption.getInt("b");

            rCaption = creator.createTextField("r", getActualValue(rCaptionInt), 10, 150, 50, true, true, validVersion, !validVersion);
            gCaption = creator.createTextField("g", getActualValue(gCaptionInt), 80, 150, 50, true, true, validVersion, !validVersion);
            bCaption = creator.createTextField("b", getActualValue(bCaptionInt), 150, 150, 50, true, true, validVersion, !validVersion);

            rCaption.textProperty().addListener(createTextFieldChangeListener(detailArea, rCaption, "caption"));
            gCaption.textProperty().addListener(createTextFieldChangeListener(detailArea, gCaption, "caption"));
            bCaption.textProperty().addListener(createTextFieldChangeListener(detailArea, bCaption, "caption"));

            Label textLabel = creator.createLabel("Color for titlebar text", 250, 120);

            JSONObject text = dwm.getJSONObject("text");

            int rTextInt = text.getInt("r");
            int gTextInt = text.getInt("g");
            int bTextInt = text.getInt("b");

            rText = creator.createTextField("r", getActualValue(rTextInt), 250, 150, 50, true, true, validVersion, !validVersion);
            gText = creator.createTextField("g", getActualValue(gTextInt), 320, 150, 50, true, true, validVersion, !validVersion);
            bText = creator.createTextField("b", getActualValue(bTextInt), 390, 150, 50, true, true, validVersion, !validVersion);

            rText.textProperty().addListener(createTextFieldChangeListener(detailArea, rText, "text"));
            gText.textProperty().addListener(createTextFieldChangeListener(detailArea, gText, "text"));
            bText.textProperty().addListener(createTextFieldChangeListener(detailArea, bText, "text"));

            Label borderLabel = creator.createLabel("Color for window border", 10, 200);

            JSONObject border = dwm.getJSONObject("border");

            int rBorderInt = border.getInt("r");
            int gBorderInt = border.getInt("g");
            int bBorderInt = border.getInt("b");

            rBorder = creator.createTextField("r", getActualValue(rBorderInt), 10, 230, 50, true, true, validVersion, !validVersion);
            gBorder = creator.createTextField("g", getActualValue(gBorderInt), 80, 230, 50, true, true, validVersion, !validVersion);
            bBorder = creator.createTextField("b", getActualValue(bBorderInt), 150, 230, 50, true, true, validVersion, !validVersion);

            rBorder.textProperty().addListener(createTextFieldChangeListener(detailArea, rBorder, "border"));
            gBorder.textProperty().addListener(createTextFieldChangeListener(detailArea, gBorder, "border"));
            bBorder.textProperty().addListener(createTextFieldChangeListener(detailArea, bBorder, "border"));

            updateCheck = root.getBoolean("update-check");
            pushNotifications = root.getBoolean("push-notifications");
            autoOpenExplorer = root.getBoolean("auto-open-explorer");
            boolean defaultDarkMode = root.getBoolean("default-dark-mode");

            Label darkModeLabel = creator.createLabel("Use dark mode (mica must be disabled)", 60, 280);
            ToggleSwitch darkModeSwitch = creator.createToggleSwitch(-8, 280, defaultDarkMode, validVersion && dwma.value != 1);

            Label checkUpdateLabel = creator.createLabel("Automatically check for updates", 60, 305);
            ToggleSwitch checkUpdateSwitch = creator.createToggleSwitch(-8, 305, updateCheck, false);

            Label autoOpenExplorerLabel = creator.createLabel("Open Explorer on file creation success", 60, 330);
            ToggleSwitch autoOpenExplorerSwitch = creator.createToggleSwitch(-8, 330, autoOpenExplorer, false);

            Label pushNotificationLabel = creator.createLabel("Get push notification for executed tasks", 60, 355);
            ToggleSwitch pushNotificationSwitch = creator.createToggleSwitch(-8, 355, pushNotifications, false);

            String windowsVersion = "You are currently on: " + System.getProperty("os.name") + " " + displayVersion;

            Node displayVersionNode = creator.createLabel(windowsVersion, 10, 387);

            if (!validVersion)
            {
                displayVersionNode = creator.createHyperlink(hostServices, windowsVersion, "https://github.com/BlockyDotJar/VorteX#why-cant-i-change-some-style-settings", 10, 387);
            }

            Button checkForUpdates = creator.createButton("Check for updates", 445, 375, 150, false);

            anchorPane.getChildren().addAll
                    (
                            detailArea, dwmLabel, dwmAttribute, immersiveDarkModeLabel, immersiveDarkModeSwitch, captionLabel,
                            rCaption, gCaption, bCaption, textLabel, rText, gText, bText, borderLabel, rBorder, gBorder, bBorder,
                            darkModeSwitch, darkModeLabel, checkUpdateSwitch, checkUpdateLabel, autoOpenExplorerSwitch, autoOpenExplorerLabel,
                            pushNotificationSwitch, pushNotificationLabel, displayVersionNode, checkForUpdates
                    );

            initDWMAttribute(scene, detailArea, dwmAttribute, immersiveDarkModeSwitch, darkModeSwitch);
            initImmersiveDarkMode(scene, detailArea, immersiveDarkModeSwitch);
            initDarkMode(scene, detailArea, darkModeSwitch);
            initCheckUpdate(detailArea, checkUpdateSwitch);
            initAutoOpenExplorer(detailArea, autoOpenExplorerSwitch);
            initPushNotification(detailArea, pushNotificationSwitch);
            initCheckForUpdates(hostServices, detailArea, script, checkForUpdates);
        });
    }

    public static void initDWMAttribute(Scene scene, TextArea detailArea, ComboBox<DWMAttribute> dwmAttribute, ToggleSwitch immersiveDarkModeSwitch, ToggleSwitch darkModeSwitch)
    {
        dwmAttribute.setOnAction(e ->
        {
            dwma = dwmAttribute.getValue();

            switch (dwma.value)
            {
                case 1 ->
                {
                    dwmAttribute.setPrefWidth(185);

                    boolean wasSelected = immersiveDarkModeSwitch.isSelected();

                    immersiveDarkModeSwitch.setSelected(false);
                    immersiveDarkModeSwitch.setDisable(true);

                    darkModeSwitch.setDisable(false);

                    scene.getStylesheets().removeAll
                            (
                                    SettingHandler.class.getResource("/assets/ui/css/styles.css").toExternalForm(),
                                    SettingHandler.class.getResource("/assets/ui/css/dark-styles.css").toExternalForm(),
                                    SettingHandler.class.getResource("/assets/ui/css/dwm-styles.css").toExternalForm(),
                                    SettingHandler.class.getResource("/assets/ui/css/dwm-dark-styles.css").toExternalForm()
                            );

                    if (!wasSelected)
                    {
                        scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/styles.css").toExternalForm());
                    }

                    List<Boolean> writeToDWMObjectList = List.of(true, true);
                    List<String> dwmSubStrList = List.of("undefined", "undefined");
                    List<String> keys = List.of("immersive-dark-mode", "window-type");
                    List<Object> values = List.of(false, dwma.value);

                    if (wasSelected)
                    {
                        darkModeSwitch.setSelected(true);

                        scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dark-styles.css").toExternalForm());

                        writeToDWMObjectList = List.of(true, true, false);
                        dwmSubStrList = List.of("undefined", "undefined", "undefined");
                        keys = List.of("immersive-dark-mode", "window-type", "default-dark-mode");
                        values = List.of(false, dwma.value, true);
                    }

                    setMicaStyle(dwma, false);

                    writeMultipleSettingsToFile(detailArea, writeToDWMObjectList, dwmSubStrList, keys, values);
                }
                case 2, 4 ->
                {
                    dwmAttribute.setPrefWidth(dwma.value == 2 ? 220 : 247);

                    immersiveDarkModeSwitch.setDisable(false);

                    boolean wasSelected = darkModeSwitch.isSelected();

                    darkModeSwitch.setSelected(false);
                    darkModeSwitch.setDisable(true);

                    scene.getStylesheets().removeAll
                            (
                                    SettingHandler.class.getResource("/assets/ui/css/styles.css").toExternalForm(),
                                    SettingHandler.class.getResource("/assets/ui/css/dark-styles.css").toExternalForm(),
                                    SettingHandler.class.getResource("/assets/ui/css/dwm-styles.css").toExternalForm(),
                                    SettingHandler.class.getResource("/assets/ui/css/dwm-dark-styles.css").toExternalForm()
                            );

                    if (wasSelected)
                    {
                        immersiveDarkModeSwitch.setSelected(true);

                        scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dwm-dark-styles.css").toExternalForm());
                        setMicaStyle(dwma, true);

                        List<Boolean> writeToDWMObjectList = List.of(true, true, false);
                        List<String> dwmSubStrList = List.of("undefined", "undefined", "undefined");
                        List<String> keys = List.of("immersive-dark-mode", "window-type", "default-dark-mode");
                        List<Object> values = List.of(true, dwma.value, false);

                        writeMultipleSettingsToFile(detailArea, writeToDWMObjectList, dwmSubStrList, keys, values);
                        return;
                    }

                    if (immersiveDarkModeSwitch.isSelected())
                    {
                        scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dwm-dark-styles.css").toExternalForm());
                        setMicaStyle(dwma, true);

                        List<Boolean> writeToDWMObjectList = List.of(true, true);
                        List<String> dwmSubStrList = List.of("undefined", "undefined");
                        List<String> keys = List.of("immersive-dark-mode", "window-type");
                        List<Object> values = List.of(true, dwma.value);

                        writeMultipleSettingsToFile(detailArea, writeToDWMObjectList, dwmSubStrList, keys, values);
                        return;
                    }

                    scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dwm-styles.css").toExternalForm());

                    setMicaStyle(dwma, false);

                    List<Boolean> writeToDWMObjectList = List.of(true, true);
                    List<String> dwmSubStrList = List.of("undefined", "undefined");
                    List<String> keys = List.of("immersive-dark-mode", "window-type");
                    List<Object> values = List.of(false, dwma.value);

                    writeMultipleSettingsToFile(detailArea, writeToDWMObjectList, dwmSubStrList, keys, values);
                }
            }
        });
    }

    public static void initImmersiveDarkMode(Scene scene, TextArea detailArea, ToggleSwitch immersiveDarkModeSwitch)
    {
        immersiveDarkModeSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
        {
            writeSettingsFile(detailArea, true, null, "immersive-dark-mode", newVal);

            scene.getStylesheets().removeAll
                    (
                            SettingHandler.class.getResource("/assets/ui/css/dwm-styles.css").toExternalForm(),
                            SettingHandler.class.getResource("/assets/ui/css/dwm-dark-styles.css").toExternalForm()
                    );

            if (newVal)
            {
                scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dwm-dark-styles.css").toExternalForm());
                DWMHandler.dwmSetBooleanValue(DWMAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, true);
                return;
            }

            scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dwm-styles.css").toExternalForm());
            DWMHandler.dwmSetBooleanValue(DWMAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, false);
        });
    }

    public static void initDarkMode(Scene scene, TextArea detailArea, ToggleSwitch darkModeSwitch)
    {
        darkModeSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
        {
            writeSettingsFile(detailArea, false, null, "default-dark-mode", newVal);

            scene.getStylesheets().removeAll
                    (
                            SettingHandler.class.getResource("/assets/ui/css/styles.css").toExternalForm(),
                            SettingHandler.class.getResource("/assets/ui/css/dark-styles.css").toExternalForm()
                    );

            if (newVal)
            {
                scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/dark-styles.css").toExternalForm());
                return;
            }

            scene.getStylesheets().add(SettingHandler.class.getResource("/assets/ui/css/styles.css").toExternalForm());
        });
    }

    public static void initCheckUpdate(TextArea detailArea, ToggleSwitch checkUpdateSwitch)
    {
        checkUpdateSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
        {
            updateCheck = newVal;
            writeSettingsFile(detailArea, false, null, "update-check", newVal);
        });
    }

    public static void initAutoOpenExplorer(TextArea detailArea, ToggleSwitch autoOpenExplorerSwitch)
    {
        autoOpenExplorerSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
        {
            autoOpenExplorer = newVal;
            writeSettingsFile(detailArea, false, null, "auto-open-explorer", newVal);
        });
    }

    public static void initPushNotification(TextArea detailArea, ToggleSwitch pushNotificationSwitch)
    {
        pushNotificationSwitch.selectedProperty().addListener((obs, oldVal, newVal) ->
        {
            pushNotifications = newVal;
            writeSettingsFile(detailArea, false, null, "push-notifications", newVal);
        });
    }

    public static void initCheckForUpdates(HostServices hostServices, TextArea detailArea, String script, Button checkForUpdates)
    {
        checkForUpdates.setOnAction(e ->
                Platform.runLater(() ->
                {
                    if (initApplicationUpdater(hostServices, detailArea, script))
                    {
                        return;
                    }

                    String title = "No new VorteX version available.";
                    String headerText = "Looks like there is a no new version of VorteX available.";
                    String contextText = "The latest version of VorteX is already installed...";

                    Alert updateAlert = creator.createAlert(Alert.AlertType.INFORMATION, title, headerText, contextText);
                    updateAlert.show();
                })
        );
    }

    private static ChangeListener<? super String> createTextFieldChangeListener(TextArea detailArea, TextField textField, String dwmSubStr)
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

            if (newVal.length() > 3)
            {
                int i = Integer.parseInt(newVal);

                if (i > 255)
                {
                    textField.setText("255");
                }
            }

            writeSettingsFile(detailArea, true, dwmSubStr, textField.getPromptText(), getActualValueAsInt(textField.getText()));

            int rCaptionInt = getActualValueAsInt(rCaption.getText());
            int gCaptionInt = getActualValueAsInt(gCaption.getText());
            int bCaptionInt = getActualValueAsInt(bCaption.getText());

            dwmSetIntValue(DWMAttribute.DWMWA_CAPTION_COLOR, -1);

            if (rCaptionInt != -1 && gCaptionInt != -1 && bCaptionInt != -1)
            {
                DWMHandler.setCaptionColor(Color.rgb(rCaptionInt, gCaptionInt, bCaptionInt));
            }

            int rTextInt = getActualValueAsInt(rText.getText());
            int gTextInt = getActualValueAsInt(gText.getText());
            int bTextInt = getActualValueAsInt(bText.getText());

            dwmSetIntValue(DWMAttribute.DWMWA_TEXT_COLOR, -1);

            if (rTextInt != -1 && gTextInt != -1 && bTextInt != -1)
            {
                DWMHandler.setTextColor(Color.rgb(rTextInt, gTextInt, bTextInt));
            }

            int rBorderInt = getActualValueAsInt(rBorder.getText());
            int gBorderInt = getActualValueAsInt(gBorder.getText());
            int bBorderInt = getActualValueAsInt(bBorder.getText());

            dwmSetIntValue(DWMAttribute.DWMWA_BORDER_COLOR, -1);

            if (rBorderInt != -1 && gBorderInt != -1 && bBorderInt != -1)
            {
                DWMHandler.setBorderColor(Color.rgb(rBorderInt, gBorderInt, bBorderInt));
            }
        };
    }

    public static String readSettings(TextArea detailArea)
    {
        try
        {
            String vortexHome = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_HOME");
            File settingsFile = new File(vortexHome + "\\settings.json");
            return Files.readString(settingsFile.toPath());
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            sendErrorPushNotification(detailArea, e);
        }
        return null;
    }

    private static void writeSettingsFile(TextArea detailArea, boolean writeToDWMObject, String dwmSubStr, String key, Object value)
    {
        try
        {
            String vortexHome = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_HOME");
            File settingsFile = new File(vortexHome + "\\settings.json");

            JSONObject root = new JSONObject(readSettings(detailArea));

            if (!writeToDWMObject)
            {
                root.put(key, value);
                writeString(settingsFile.toPath(), root.toString(4));
                return;
            }

            JSONObject dwm = root.getJSONObject("dwm");

            if (dwmSubStr == null)
            {
                dwm.put(key, value);
                writeString(settingsFile.toPath(), root.toString(4));
                return;
            }

            JSONObject dwmSub = dwm.getJSONObject(dwmSubStr);
            dwmSub.put(key, value);

            writeString(settingsFile.toPath(), root.toString(4));
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            sendErrorPushNotification(detailArea, e);
        }
    }

    private static void writeMultipleSettingsToFile(TextArea detailArea, List<Boolean> writeToDWMObjectList, List<String> dwmSubStrList, List<String> keys, List<Object> values)
    {
        try
        {
            String vortexHome = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\VorteX", "VorteX_HOME");
            File settingsFile = new File(vortexHome + "\\settings.json");
            String jsonStr = Files.readString(settingsFile.toPath());

            JSONObject root = new JSONObject(jsonStr);

            for (int i = 0; i < keys.size(); i++)
            {
                boolean writeToDWMObject = writeToDWMObjectList.get(i);

                String dwmSubStr = dwmSubStrList.get(i);
                String key = keys.get(i);
                Object value = values.get(i);

                if (!writeToDWMObject)
                {
                    root.put(key, value);
                    writeString(settingsFile.toPath(), root.toString(4));
                    continue;
                }

                JSONObject dwm = root.getJSONObject("dwm");

                if (dwmSubStr.equals("undefined"))
                {
                    dwm.put(key, value);
                    writeString(settingsFile.toPath(), root.toString(4));
                    continue;
                }

                JSONObject dwmSub = dwm.getJSONObject(dwmSubStr);
                dwmSub.put(key, value);
            }

            writeString(settingsFile.toPath(), root.toString(4));
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            sendErrorPushNotification(detailArea, e);
        }
    }

    private static String getActualValue(int val)
    {
        return val == -1 ? null : String.valueOf(val);
    }

    private static int getActualValueAsInt(String val)
    {
        return val == null || val.isEmpty() ? -1 : Integer.parseInt(val);
    }
}
