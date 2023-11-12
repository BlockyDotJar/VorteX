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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public interface IShell32 extends Shell32
{
    IShell32 INSTANCE = Native.load("shell32", IShell32.class);
    WinNT.HRESULT SHCreateItemFromParsingName(WString path, Pointer pointer, Guid.REFIID guid, PointerByReference reference);
}
