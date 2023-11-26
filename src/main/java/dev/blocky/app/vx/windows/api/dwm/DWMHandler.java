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

public class DWMHandler
{
    public static class WindowHandle
    {
        private final WinDef.HWND value;

        private WindowHandle(WinDef.HWND hwnd)
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

    public static boolean dwmSetBooleanValue(WindowHandle handle, DWMAttribute attribute, boolean value)
    {
        if (handle == null)
        {
            return false;
        }

        return isOk
                (
                        DWMSupport.INSTANCE.DwmSetWindowAttribute
                                (
                                        handle.value,
                                        attribute.value,
                                        new WinDef.BOOLByReference(new WinDef.BOOL(value)),
                                        WinDef.BOOL.SIZE
                                )
                );
    }

    public static boolean dwmSetIntValue(WindowHandle handle, DWMAttribute attribute, int value)
    {
        if (handle == null)
        {
            return false;
        }

        return isOk
                (
                        DWMSupport.INSTANCE.DwmSetWindowAttribute
                                (
                                        handle.value,
                                        attribute.value,
                                        new WinDef.DWORDByReference(new WinDef.DWORD(value)),
                                        WinDef.DWORD.SIZE
                                )
                );
    }

    public static WindowHandle findWindowHandle(String windowTitle)
    {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

        if (hwnd != null)
        {
            return new WindowHandle(hwnd);
        }
        return null;
    }

    public static boolean setBorderColor(WindowHandle handle, Color color)
    {
        return dwmSetIntValue(handle, DWMAttribute.DWMWA_BORDER_COLOR, RGB(color));
    }

    public static boolean setCaptionColor(WindowHandle handle, Color color)
    {
        return dwmSetIntValue(handle, DWMAttribute.DWMWA_CAPTION_COLOR, RGB(color));
    }

    public static boolean setTextColor(WindowHandle handle, Color color)
    {
        return dwmSetIntValue(handle, DWMAttribute.DWMWA_TEXT_COLOR, RGB(color));
    }

    public static void setMicaMaterial(DWMAttribute dwma, boolean useImmersiveDarkMode)
    {
        DWMHandler.WindowHandle handle = DWMHandler.findWindowHandle("VorteX");
        DWMHandler.dwmSetBooleanValue(handle, DWMAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, useImmersiveDarkMode);

        if (!DWMHandler.dwmSetIntValue(handle, DWMAttribute.DWMWA_SYSTEMBACKDROP_TYPE, dwma.value))
        {
            DWMHandler.dwmSetBooleanValue(handle, DWMAttribute.DWMWA_MICA_EFFECT, true);
        }
    }

    private static int floatingTo8Bit(double n)
    {
        return (int) Math.min(255.0, Math.max(n * 255.0, 0.0));
    }

    private static boolean isOk(WinNT.HRESULT result)
    {
        return WinNT.HRESULT.compare(result, W32Errors.S_OK) == 0;
    }

    static int RGB(Color color)
    {
        return (floatingTo8Bit(color.getBlue()) << 16)
                | (floatingTo8Bit(color.getGreen()) << 8)
                | floatingTo8Bit(color.getRed());
    }
}
