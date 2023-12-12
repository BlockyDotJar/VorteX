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
import javafx.scene.paint.Color;
import org.json.JSONObject;

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
        return dwmSetIntValue(DWMAttribute.DWMWA_BORDER_COLOR, RGB(color));
    }

    public static boolean setCaptionColor(Color color)
    {
        return dwmSetIntValue(DWMAttribute.DWMWA_CAPTION_COLOR, RGB(color));
    }

    public static boolean setTextColor(Color color)
    {
        return dwmSetIntValue(DWMAttribute.DWMWA_TEXT_COLOR, RGB(color));
    }

    public static void setMicaStyle(DWMAttribute dwma, boolean useImmersiveDarkMode)
    {
        dwmSetBooleanValue(DWMAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, useImmersiveDarkMode);

        if (!dwmSetIntValue(DWMAttribute.DWMWA_SYSTEMBACKDROP_TYPE, dwma.value))
        {
            dwmSetBooleanValue(DWMAttribute.DWMWA_MICA_EFFECT, true);
        }
    }

    public static void handleStyleSettings(JSONObject dwm)
    {
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
    }

    private static boolean isOk(WinNT.HRESULT result)
    {
        return WinNT.HRESULT.compare(result, W32Errors.S_OK) == 0;
    }

    private static int floatingTo8Bit(double n)
    {
        return (int) Math.min(255.0, Math.max(n * 255.0, 0.0));
    }

    private static int RGB(Color color)
    {
        return (floatingTo8Bit(color.getBlue()) << 16)
                | (floatingTo8Bit(color.getGreen()) << 8)
                | floatingTo8Bit(color.getRed());
    }
}
