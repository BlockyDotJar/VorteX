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
package dev.blocky.app.vx.codec;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashSet;

public class Base32Codec
{
    public static void encodeAndRenameFile(ZipFile zipFile) throws ZipException
    {
        for (FileHeader fileHeader : zipFile.getFileHeaders())
        {
            String fileName = fileHeader.getFileName();
            String[] split = fileName.split("/");

            Base32 base32 = new Base32();

            String lastPart = split[split.length - 1];

            String ecLp = base32.encodeAsString(lastPart.getBytes());

            if (!fileName.endsWith("/"))
            {
                String extension = FilenameUtils.getExtension(lastPart);
                String lpRaw = lastPart.replace("." + extension, "");

                ecLp = base32.encodeAsString(lpRaw.getBytes()) + "." + extension;
            }

            String fStr = fileName.replace(lastPart, ecLp);

            zipFile.renameFile(fileHeader, fStr);
        }
    }

    public static void decodeAndExtractFile(ZipFile zipFile, HashSet<FileHeader> fileHeaders, String destinationPath) throws ZipException
    {
        for (FileHeader fileHeader : fileHeaders)
        {
            String fileName = fileHeader.getFileName();
            String[] split = fileName.split("/");

            Base32 base32 = new Base32();

            StringBuilder decodedPath = new StringBuilder();

            for (String part : split)
            {
                File file;

                if (FilenameUtils.getExtension(part).isBlank())
                {
                    String decodedFilename = new String(base32.decode(part.getBytes()));

                    decodedPath.append(decodedFilename).append("/");

                    String realPath = decodedPath.toString().replace("/", "\\");
                    file = new File(destinationPath + "\\" + realPath);

                    if (!file.exists())
                    {
                        file.mkdirs();
                    }

                    continue;
                }

                String extension = FilenameUtils.getExtension(part);
                String lpRaw = part.replace("." + extension, "");

                decodedPath.append(new String(base32.decode(lpRaw.getBytes()))).append(".").append(extension);
            }

            String realPath = decodedPath.toString().replace("/", "\\");
            File file = new File(destinationPath + "\\" + realPath);

            if (!file.exists())
            {
                zipFile.extractFile(fileHeader, destinationPath, decodedPath.toString());
            }
        }
    }

    public static String decodeEncodedPart(String part)
    {
        Base32 base32 = new Base32();

        if (FilenameUtils.getExtension(part).isBlank())
        {
            return new String(base32.decode(part.getBytes()));
        }

        String extension = FilenameUtils.getExtension(part);
        String fhRaw = part.replace("." + extension, "");

        return new String(base32.decode(fhRaw.getBytes())) + "." + extension;
    }
}
