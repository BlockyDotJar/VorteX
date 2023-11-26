package dev.blocky.app.vx.handler;

import javafx.scene.control.TextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static dev.blocky.app.vx.handler.ActionHandler.invalidAction;

public class TrayIconHandler
{
    public static void sendErrorPushNotification(TextArea detailArea, Exception exception)
    {
        try
        {
            if (SettingHandler.pushNotifications)
            {
                SystemTray tray = SystemTray.getSystemTray();

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

            if (SettingHandler.pushNotifications)
            {
                TrayIconHandler.sendErrorPushNotification(detailArea, e);
            }
        }
    }
}
