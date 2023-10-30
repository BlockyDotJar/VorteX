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

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import static dev.blocky.app.vx.handler.ActionHandler.lastUsedButton;

public class ArchiveOpeningHandler
{
    public static void initOpenArchive(AnchorPane anchorPane, TextArea detailArea, Button openArchive)
    {
        openArchive.setOnAction(event ->
        {
            lastUsedButton.setDisable(false);
            lastUsedButton = openArchive;
            openArchive.setDisable(true);

            anchorPane.getChildren().clear();
            anchorPane.getChildren().add(detailArea);
        });
    }
}
