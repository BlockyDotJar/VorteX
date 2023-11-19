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
package dev.blocky.app.vx.windows.api.dwm;

public class DWMWindow
{
    public static void setMicaMaterial(String windowTitle, DWMAttribute dwma, boolean useImmersiveDarkMode)
    {
        DWMHandler.WindowHandle handle = DWMHandler.findWindowHandle(windowTitle);
        DWMHandler.dwmSetBooleanValue(handle, DWMAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, useImmersiveDarkMode);

        if (!DWMHandler.dwmSetIntValue(handle, DWMAttribute.DWMWA_SYSTEMBACKDROP_TYPE, dwma.value))
        {
            DWMHandler.dwmSetBooleanValue(handle, DWMAttribute.DWMWA_MICA_EFFECT, true);
        }
    }
}
