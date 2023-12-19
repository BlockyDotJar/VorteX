/**
 * custom_window - A small collection of utility methods to customize a JavaFX stage.
 * Copyright (C) 2022 M. Oguz Tas (mimoguz - https://github.com/mimoguz)
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
package dev.blocky.app.vx.windows.api.dwm;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.util.List;

import static dev.blocky.app.vx.handler.SettingHandler.getActualValueAsInt;

public class DWMHandler
{
    public static class Window
    {
        private final WinDef.HWND value;

        private Window(WinDef.HWND hwnd)
        {
            value = hwnd;
        }
    }

    private interface DWMSupport extends Library
    {
        DWMSupport INSTANCE = Native.load("dwmapi", DWMSupport.class);

        WinNT.HRESULT DwmSetWindowAttribute
                (
                        WinDef.HWND hwnd,
                        int dwAttribute,
                        PointerType pvAttribute,
                        int cbAttribute
                );
    }

    public static boolean dwmSetBooleanValue(DWMAttribute attribute, boolean value)
    {
        Window window = findWindow();

        if (window == null)
        {
            return false;
        }

        return isOk
                (
                        DWMSupport.INSTANCE.DwmSetWindowAttribute
                                (
                                        window.value,
                                        attribute.value,
                                        new WinDef.BOOLByReference(new WinDef.BOOL(value)),
                                        WinDef.BOOL.SIZE
                                )
                );
    }

    public static boolean dwmSetIntValue(DWMAttribute attribute, int value)
    {
        Window window = findWindow();

        if (window == null)
        {
            return false;
        }

        return isOk
                (
                        DWMSupport.INSTANCE.DwmSetWindowAttribute
                                (
                                        window.value,
                                        attribute.value,
                                        new WinDef.DWORDByReference(new WinDef.DWORD(value)),
                                        WinDef.DWORD.SIZE
                                )
                );
    }

    public static Window findWindow()
    {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, "VorteX");

        if (hwnd != null)
        {
            return new Window(hwnd);
        }
        return null;
    }

    public static boolean setBorderColor(Color color)
    {
        return dwmSetIntValue(DWMAttribute.DWMWA_BORDER_COLOR, rgb(color));
    }

    public static boolean setCaptionColor(Color color)
    {
        return dwmSetIntValue(DWMAttribute.DWMWA_CAPTION_COLOR, rgb(color));
    }

    public static boolean setTextColor(Color color)
    {
        return dwmSetIntValue(DWMAttribute.DWMWA_TEXT_COLOR, rgb(color));
    }

    public static void setMicaStyle(DWMAttribute dwma, boolean useImmersiveDarkMode)
    {
        dwmSetBooleanValue(DWMAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, useImmersiveDarkMode);

        if (!dwmSetIntValue(DWMAttribute.DWMWA_SYSTEMBACKDROP_TYPE, dwma.value))
        {
            dwmSetBooleanValue(DWMAttribute.DWMWA_MICA_EFFECT, true);
        }
    }

    public static void handleStyleSettings(AnchorPane anchorPane, JSONObject root)
    {
        JSONObject dwm = root.getJSONObject("dwm");
        JSONObject caption = dwm.getJSONObject("caption");

        int rCaptionInt = caption.getInt("r");
        int gCaptionInt = caption.getInt("g");
        int bCaptionInt = caption.getInt("b");

        if (rCaptionInt != -1 && gCaptionInt != -1 && bCaptionInt != -1)
        {
            setCaptionColor(Color.rgb(rCaptionInt, gCaptionInt, bCaptionInt));
        }

        JSONObject text = dwm.getJSONObject("text");

        int rTextInt = text.getInt("r");
        int gTextInt = text.getInt("g");
        int bTextInt = text.getInt("b");

        if (rTextInt != -1 && gTextInt != -1 && bTextInt != -1)
        {
            setTextColor(Color.rgb(rTextInt, gTextInt, bTextInt));
        }

        JSONObject border = dwm.getJSONObject("border");

        int rBorderInt = border.getInt("r");
        int gBorderInt = border.getInt("g");
        int bBorderInt = border.getInt("b");

        if (rBorderInt != -1 && gBorderInt != -1 && bBorderInt != -1)
        {
            setBorderColor(Color.rgb(rBorderInt, gBorderInt, bBorderInt));
        }

        JSONObject fill = root.getJSONObject("window-fill");

        int rFillInt = fill.getInt("r");
        int gFillInt = fill.getInt("g");
        int bFillInt = fill.getInt("b");

        anchorPane.setBackground(Background.EMPTY);

        if (rFillInt != -1 && gFillInt != -1 && bFillInt != -1)
        {
            anchorPane.setBackground(Background.fill(Color.rgb(rFillInt, gFillInt, bFillInt)));
        }
    }

    public static void handleStyle(AnchorPane anchorPane, List<TextField> textFields, boolean defaultDarkMode, boolean usesMica)
    {
        int rCaptionInt = getActualValueAsInt(textFields.get(0).getText());
        int gCaptionInt = getActualValueAsInt(textFields.get(1).getText());
        int bCaptionInt = getActualValueAsInt(textFields.get(2).getText());

        if (usesMica)
        {
            dwmSetIntValue(DWMAttribute.DWMWA_CAPTION_COLOR, -1);
        }

        if (!usesMica)
        {
            if (defaultDarkMode)
            {
                setCaptionColor(Color.rgb(34, 34, 34));
            }

            if (!defaultDarkMode)
            {
                setCaptionColor(Color.rgb(230, 230, 250));
            }
        }

        if (rCaptionInt != -1 && gCaptionInt != -1 && bCaptionInt != -1)
        {
            setCaptionColor(Color.rgb(rCaptionInt, gCaptionInt, bCaptionInt));
        }

        int rTextInt = getActualValueAsInt(textFields.get(3).getText());
        int gTextInt = getActualValueAsInt(textFields.get(4).getText());
        int bTextInt = getActualValueAsInt(textFields.get(5).getText());

        dwmSetIntValue(DWMAttribute.DWMWA_TEXT_COLOR, -1);

        if (rTextInt != -1 && gTextInt != -1 && bTextInt != -1)
        {
            setTextColor(Color.rgb(rTextInt, gTextInt, bTextInt));
        }

        int rBorderInt = getActualValueAsInt(textFields.get(6).getText());
        int gBorderInt = getActualValueAsInt(textFields.get(7).getText());
        int bBorderInt = getActualValueAsInt(textFields.get(8).getText());

        dwmSetIntValue(DWMAttribute.DWMWA_BORDER_COLOR, -1);

        if (rBorderInt != -1 && gBorderInt != -1 && bBorderInt != -1)
        {
            setBorderColor(Color.rgb(rBorderInt, gBorderInt, bBorderInt));
        }

        int rFillInt = getActualValueAsInt(textFields.get(9).getText());
        int gFillInt = getActualValueAsInt(textFields.get(10).getText());
        int bFillInt = getActualValueAsInt(textFields.get(11).getText());

        anchorPane.setBackground(Background.EMPTY);

        if (rFillInt != -1 && gFillInt != -1 && bFillInt != -1)
        {
            anchorPane.setBackground(Background.fill(Color.rgb(rFillInt, gFillInt, bFillInt)));
        }
    }

    public static boolean usesCustomization(TextField rField, TextField gField, TextField bField)
    {
        int rFieldInt = getActualValueAsInt(rField.getText());
        int gFieldInt = getActualValueAsInt(gField.getText());
        int bFieldInt = getActualValueAsInt(bField.getText());
        return rFieldInt != -1 && gFieldInt != -1 && bFieldInt != -1;
    }

    private static boolean isOk(WinNT.HRESULT result)
    {
        return WinNT.HRESULT.compare(result, W32Errors.S_OK) == 0;
    }

    private static int floatingTo8Bit(double number)
    {
        return (int) Math.min(255.0, Math.max(number * 255.0, 0.0));
    }

    private static int rgb(Color color)
    {
        return (floatingTo8Bit(color.getBlue()) << 16)
                | (floatingTo8Bit(color.getGreen()) << 8)
                | floatingTo8Bit(color.getRed());
    }
}
