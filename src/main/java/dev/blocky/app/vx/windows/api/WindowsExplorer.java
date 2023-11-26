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
package dev.blocky.app.vx.windows.api;

import dev.blocky.app.vx.handler.SettingHandler;
import dev.blocky.app.vx.handler.TrayIconHandler;
import javafx.scene.control.TextArea;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.StringWriter;

import static dev.blocky.app.vx.handler.ActionHandler.invalidAction;
import static java.nio.charset.StandardCharsets.UTF_8;

public class WindowsExplorer
{
    public static void openDirectoryAndHighlightFile(TextArea detailArea, File file)
    {
        try
        {
            ProcessBuilder builder = new ProcessBuilder().command
                    (
                            "explorer.exe",
                            "/select,",
                            "\"" + file.getAbsolutePath() + "\""
                    );

            Process process = builder.start();

            if (process.waitFor() != 0)
            {
                StringWriter writer = new StringWriter();
                IOUtils.copy(process.getErrorStream(), writer, UTF_8);

                String stackTrace = writer.toString();

                if (!stackTrace.isBlank())
                {
                    invalidAction(detailArea, writer.toString());

                    if (SettingHandler.pushNotifications)
                    {
                        TrayIconHandler.sendErrorPushNotification(detailArea, new IllegalStateException(writer.toString()));
                    }
                }
            }
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));

            if (SettingHandler.pushNotifications)
            {
                TrayIconHandler.sendErrorPushNotification(detailArea, e);
            }
        }
    }
}
