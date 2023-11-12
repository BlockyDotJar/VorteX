/**
 * JIconExtractReloaded - Extract icons in whatever size you want from every file no matter if folder or file.
 * Copyright (C) 31.10.2019 MrMarnic (https://github.com/MrMarnic)
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
package dev.blocky.app.vx.windows.api.registry;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.PointerByReference;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.File;

public class FileIconImage
{
    public static Image getIconForFile(int width, int height, File file)
    {
        return getIconForFile(width, height, file.getAbsolutePath());
    }

    public static Image getIconForFile(int width, int height, String fileName)
    {
        WinDef.HBITMAP hbitmap = getHBITMAPForFile(width, height, fileName);

        WinGDI.BITMAP bitmap = new WinGDI.BITMAP();

        try
        {
            int s = GDI32.INSTANCE.GetObject(hbitmap, bitmap.size(), bitmap.getPointer());

            if (s > 0)
            {
                bitmap.read();

                int w = bitmap.bmWidth.intValue();
                int h = bitmap.bmHeight.intValue();

                WinDef.HDC hdc = User32.INSTANCE.GetDC(null);

                WinGDI.BITMAPINFO bitmapinfo = new WinGDI.BITMAPINFO();

                bitmapinfo.bmiHeader.biSize = bitmapinfo.bmiHeader.size();

                if (0 == GDI32.INSTANCE.GetDIBits(hdc, hbitmap, 0, 0, Pointer.NULL, bitmapinfo, WinGDI.DIB_RGB_COLORS))
                {
                    throw new IllegalArgumentException("GetDIBits should not return 0.");
                }

                bitmapinfo.read();

                Memory lpPixels = new Memory(bitmapinfo.bmiHeader.biSizeImage);

                bitmapinfo.bmiHeader.biCompression = WinGDI.BI_RGB;
                bitmapinfo.bmiHeader.biHeight = -h;

                if (0 == GDI32.INSTANCE.GetDIBits(hdc, hbitmap, 0, bitmapinfo.bmiHeader.biHeight, lpPixels, bitmapinfo, WinGDI.DIB_RGB_COLORS))
                {
                    throw new IllegalArgumentException("GetDIBits should not return 0.");
                }

                int[] colorArray = lpPixels.getIntArray(0, w * h);

                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                bi.setRGB(0, 0, w, h, colorArray, 0, w);

                return SwingFXUtils.toFXImage(bi, null);
            }
        }
        finally
        {
            GDI32.INSTANCE.DeleteObject(hbitmap);
        }
        return null;
    }

    public static WinDef.HBITMAP getHBITMAPForFile(int width, int height, String fileName)
    {
        WinNT.HRESULT h1 = Ole32.INSTANCE.CoInitialize(null);

        if (COMUtils.SUCCEEDED(h1))
        {
            PointerByReference factory = new PointerByReference();

            WinNT.HRESULT h2 = IShell32.INSTANCE.SHCreateItemFromParsingName(new WString(fileName), null, new Guid.REFIID(new Guid.IID("BCC18B79-BA16-442F-80C4-8A59C30C463B")), factory);

            if (COMUtils.SUCCEEDED(h2))
            {
                ShellItemImageFactory imageFactory = new ShellItemImageFactory(factory.getValue());

                PointerByReference hBitmapPointer = new PointerByReference();

                WinNT.HRESULT h3 = imageFactory.GetImage(new SIZEByValue(width, height), 0, hBitmapPointer);

                if (COMUtils.SUCCEEDED(h3))
                {
                    return new WinDef.HBITMAP(hBitmapPointer.getValue());
                }
                imageFactory.Release();
            }
        }
        return null;
    }
}
