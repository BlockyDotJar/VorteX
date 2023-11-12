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

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public class ShellItemImageFactory extends Unknown
{
    public ShellItemImageFactory(Pointer pvInstance)
    {
        super(pvInstance);
    }

    public WinNT.HRESULT GetImage(
            SIZEByValue size,
            int flags,
            PointerByReference bitmap
    )
    {
        return (WinNT.HRESULT) _invokeNativeObject(3,
                new Object[]{this.getPointer(), size, flags, bitmap}, WinNT.HRESULT.class);
    }
}
