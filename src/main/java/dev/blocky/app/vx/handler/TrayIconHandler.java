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

import javafx.scene.control.TextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import static dev.blocky.app.vx.handler.ActionHandler.invalidAction;
import static dev.blocky.app.vx.handler.SettingHandler.pushNotifications;

public class TrayIconHandler
{
    private static final SystemTray tray = SystemTray.getSystemTray();

    public static void sendPushNotification(TextArea detailArea, ActionListener actionListener, TrayIcon.MessageType messageType, String caption, String text)
    {
        try
        {
            if (pushNotifications)
            {
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                TrayIcon trayIcon = new TrayIcon(image);
                trayIcon.setImageAutoSize(true);

                // TODO: Fix not executing action listener.
                trayIcon.addActionListener(actionListener);

                tray.add(trayIcon);

                trayIcon.displayMessage(caption, text, messageType);

                TimeUnit.MILLISECONDS.sleep(500);

                tray.remove(trayIcon);
            }
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            sendErrorPushNotification(detailArea, e);
        }
    }

    public static void sendErrorPushNotification(TextArea detailArea, Exception exception)
    {
        try
        {
            if (pushNotifications)
            {
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

                TrayIcon trayIcon = new TrayIcon(image);
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);

                String caption = exception.getClass().getName();
                String text = exception.getMessage();

                trayIcon.displayMessage(caption, text, TrayIcon.MessageType.ERROR);

                TimeUnit.MILLISECONDS.sleep(500);

                tray.remove(trayIcon);
            }
        }
        catch (Exception e)
        {
            invalidAction(detailArea, ExceptionUtils.getStackTrace(e));
            sendErrorPushNotification(detailArea, e);
        }
    }
}
