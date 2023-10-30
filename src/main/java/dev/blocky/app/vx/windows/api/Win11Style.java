package dev.blocky.app.vx.windows.api;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Win11Style
{
    public static class WindowHandle
    {
        private final WinDef.HWND value;

        private WindowHandle(WinDef.HWND hwnd)
        {
            value = hwnd;
        }
    }

    private interface DwmSupport extends Library
    {
        DwmSupport INSTANCE = Native.load("dwmapi", DwmSupport.class);

        WinNT.HRESULT DwmSetWindowAttribute
                (
                        WinDef.HWND hwnd,
                        int dwAttribute,
                        PointerType pvAttribute,
                        int cbAttribute
                );
    }

    public static boolean dwmSetBooleanValue(WindowHandle handle, DwmAttribute attribute, boolean value)
    {
        if (handle == null)
        {
            return false;
        }

        return isOk
                (
                        DwmSupport.INSTANCE.DwmSetWindowAttribute
                                (
                                        handle.value,
                                        attribute.value,
                                        new WinDef.BOOLByReference(new WinDef.BOOL(value)),
                                        WinDef.BOOL.SIZE
                                )
                );
    }

    public static boolean dwmSetIntValue(WindowHandle handle, DwmAttribute attribute, int value)
    {
        if (handle == null)
        {
            return false;
        }

        return isOk
                (
                        DwmSupport.INSTANCE.DwmSetWindowAttribute
                                (
                                        handle.value,
                                        attribute.value,
                                        new WinDef.DWORDByReference(new WinDef.DWORD(value)),
                                        WinDef.DWORD.SIZE
                                )
                );
    }

    public static WindowHandle findWindowHandle(Stage stage)
    {
        String title = stage.getTitle();
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, title);

        if (hwnd != null)
        {
            return new WindowHandle(hwnd);
        }
        return null;
    }

    public static boolean setBorderColor(final WindowHandle handle, Color color)
    {
        return dwmSetIntValue(handle, DwmAttribute.DWMWA_BORDER_COLOR, RGB(color));
    }

    public static boolean setCaptionColor(final WindowHandle handle, final Color color)
    {
        return dwmSetIntValue(handle, DwmAttribute.DWMWA_CAPTION_COLOR, RGB(color));
    }

    public static boolean setTextColor(final WindowHandle handle, final Color color)
    {
        return dwmSetIntValue(handle, DwmAttribute.DWMWA_TEXT_COLOR, RGB(color));
    }

    private static int floatingTo8Bit(final double n)
    {
        return (int) Math.min(255.0, Math.max(n * 255.0, 0.0));
    }

    private static boolean isOk(final WinNT.HRESULT result)
    {
        return WinNT.HRESULT.compare(result, W32Errors.S_OK) == 0;
    }

    private static int RGB(final Color color)
    {
        return (floatingTo8Bit(color.getBlue()) << 16)
                | (floatingTo8Bit(color.getGreen()) << 8)
                | floatingTo8Bit(color.getRed());
    }
}
