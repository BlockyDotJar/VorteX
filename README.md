[download-shield]: https://img.shields.io/github/v/release/BlockyDotJar/VorteX
[download]: https://github.com/BlockyDotJar/VorteX/releases/latest

[license-shield]: https://img.shields.io/badge/License-GPL%203.0-white.svg
[license]: https://github.com/BlockyDotJar/VorteX/tree/main/LICENSE

[discord-invite-shield]: https://discord.com/api/guilds/876766868864647188/widget.png
[discord-invite]: https://discord.gg/FnGFbzCw2r

<img align="right" src="https://github.com/BlockyDotJar/VorteX/blob/main/src/main/resources/assets/icons/icon.png?raw=true" alt="Cartoon like monitor with purple border, blue background and the Microsoft logo." height="100" width="100">

# VorteX

[ ![download-shield][] ][download] [ ![license-shield][] ][license] [ ![discord-invite-shield][] ][discord-invite]

VorteX is a general utility program written in Java. You can create, open and extract password encrypted and Base32 encoded archive files. With VorteX you can also create and read many different barcode types.

<!--TODO: Add more fancy stuff here-->

## Supported Platforms

<!--TODO: Create contributing wiki and GitHub templates-->

Currently only **Windows** is supported. There might be a general Linux version of VorteX, but there probably won't be a macOS version, but that lies in the future.
<br>If you really want this application on macOS or on a Linux Kernel based OS and you do have experience with Java and the Kernel/API of the OS you are using, feel free to fork this repository.
See [license](https://github.com/BlockyDotJar/VorteX/blob/main/LICENSE) and [contribution](https://github.com/BlockyDotJar/VorteX/wiki/Contributing-to-VorteX) page.

## Why can't I change some settings?

<!--TODO: Add some images-->

These settings:

* Choose your mica material style
* Use immersive dark mode
* Color for titlebar
* Color for titlebar text
* Color for window border

are limited to **Windows 11 22H2 and later versions**. (you need at least build **22000** for mica **alt** and at least build **22621** for mica)
<br>That means, that you are not able to use mica material on **lower** Windows versions. (Windows **10** is generally not supported and Windows **11 21H1/21H2** are also not yet supported)
<br>You can see your current Windows display version if you take a look at the settings menu under **Get push notifications for executed tasks**.
<br>[Read this](https://support.microsoft.com/en-us/windows/which-version-of-windows-operating-system-am-i-running-628bec99-476a-2c13-5296-9dd081cdd808) if you want to know, where to find the build version of your OS.

## What is a vxar file and how is it constructed?

The **vxar** file format is a **VorteX Archive** and doesn't work that different compared to a [**zip**](https://en.wikipedia.org/wiki/ZIP_(file_format)) archive.
<br>It is used to compress and archive files and folders with password encryption. (after [AES - Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)).
<br>All files and folders in the archive do also have encoded filenames. (after [Base32](https://en.wikipedia.org/wiki/Base32) codec)
<br>Technically you could open this type of file with popular applications like [7zip](https://7-zip.de/index.html) or [WinRAR](https://www.win-rar.com/start.html), but like said above, the filenames are encoded, so you are probably unable to read these.

## Windows Defender

First of all: **yes**, this application is **safe** to install/use as you can see in the applications code.

Here are some questions you might want to ask me:

<details>
    <summary>Why does a <b>Microsoft SmartScreen</b> warning appear after opening the <b>installer</b> or why do you want to exclude the <b>VorteX installation directory</b> and the <b>vxar.exe</b> process from Windows Defender?</summary>
    <hr>
    There are a view reasons for this:
    <ol>
        <li>I don't want to pay hundreds of dollars a year for a <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/dashboard/code-signing-cert-manage#get-or-renew-a-code-signing-certificate">Microsoft trusted code-signing certificate (EV/OV)</a> for this small application</li>
        <li>Submitting this application for <a href="https://www.microsoft.com/en-us/wdsi/filesubmission">malware analysis to Microsoft</a> becomes very time-consuming, and it is very annoying to submit and wait for days or even weeks for every single version to be approved by Microsoft</li>
    </ol>
    Read more about this <a href="https://stackoverflow.com/a/51113771">here</a> and <a href="https://stackoverflow.com/a/66582477">here</a>.
    <br><br><b>So I am forced to exclude this stuff from Windows Defender?</b>
    <br><b>No</b>, you aren't. This is only needed if Windows Defender acts weirdly and detects some virus in the exe. Yeah, classic Microsoft application. It is really annoying for me as a developer that Windows Defender randomly detects non-signed '.exe' files as malicious software, but excluding all of this from Windows Defender is the only simple and inexpensive way of preventing the program from randomly being deleted or blacklisted.
    <br><br>If you don't feel comfortable with this solution, you can try to remove the Windows Defender exclusion (if you have already installed VorteX with the exclusion) with <a href="https://support.microsoft.com/en-au/topic/what-are-exclusions-in-windows-security-8b248399-5e63-4a4b-897f-52ea2dddb962#ID0EDF">this steps</a>.
    <br><br>If everything works and the Windows Defender now doesn't weirdly detect something malicious, you are now safe to use VorteX without the exclusion! (There sadly is no guaranty, that this lasts forever)
    <br>I actually don't know if other Antiviruses like Norton or McAfee are also detecting anything weird going on or if it's just Windows Defender being weird. (If that is true please open an issue here on GitHub and let me know)
</details>

## Dependencies

This project is based on **Java 17**.
<br>All dependencies and plugins are managed automatically by Gradle.
<br>A JDK is packed into the executable file, so no manual installation needed.

* (**kotlin**) stdlib
    * Version: **v1.9.21**
    * [Github](https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib)
* javafx-base (**:win**)
    * Version: **v21.0.1**
    * [Github](https://github.com/openjdk/jfx/tree/master/modules/javafx.base)
* javafx-graphics (**:win**)
    * Version: **v21.0.1**
    * [Github](https://github.com/openjdk/jfx/tree/master/modules/javafx.graphics)
* javafx-controls (**:win**)
    * Version: **v21.0.1**
    * [Github](https://github.com/openjdk/jfx/tree/master/modules/javafx.controls)
* javafx-media (**:win**)
    * Version: **v21.0.1**
    * [Github](https://github.com/openjdk/jfx/tree/master/modules/javafx.media)
* javafx-web (**:win**)
    * Version: **v21.0.1**
    * [Github](https://github.com/openjdk/jfx/tree/master/modules/javafx.web)
* javafx-swing (**:win**)
    * Version: **v21.0.1**
    * [Github](https://github.com/openjdk/jfx/tree/master/modules/javafx.swing)
* jna-platform
    * Version: **v5.14.0**
    * [Github](https://github.com/java-native-access/jna/tree/master/contrib/platform/src/com/sun/jna/platform)
* github-api
    * Version: **v1.318**
    * [Github](https://github.com/hub4j/github-api)
* json
    * Version: **v20231013**
    * [Github](https://github.com/stleary/JSON-java)
* zip4j
    * Version: **v2.11.5**
    * [Github](https://github.com/srikanth-lingala/zip4j)
* (**zxing**) core
    * Version: **v3.5.2**
    * [Github](https://github.com/zxing/zxing/tree/master/core)
* (**zxing**) javase
    * Version: **v3.5.2**
    * [Github](https://github.com/zxing/zxing/tree/master/javase)
* controlsfx
    * Version: **v11.2.0**
    * [Github](https://github.com/controlsfx/controlsfx)
* commons-lang3
    * Version: **v3.14.0**
    * [Github](https://github.com/apache/commons-lang)
* commons-io
    * Version: **v2.15.1**
    * [Github](https://github.com/apache/commons-io)
* commons-codec
    * Version: **v1.16.0**
    * [Github](https://github.com/apache/commons-codec)

## Plugins

* versions
    * Version: **v0.50.0**
    * [Github](https://github.com/ben-manes/gradle-versions-plugin)
* launch4j
    * Version: **v3.0.5**
    * [Github](https://github.com/TheBoegl/gradle-launch4j)
* (**kotlin**) jvm
    * Version: **v1.9.21**
    * [Github](https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib/jvm)

<!--TODO: Add other used tools-->

## LICENSE

This project is licensed under [GNU General Public License Version 3](https://www.gnu.org/licenses/gpl-3.0.en.html).

If you develop a new program, and you want it to be of the greatest possible use to the public, the best way to achieve this is to make it free software which everyone can redistribute and change under these terms.
<br>

To do so, attach the following notices to the program. It is safest to attach them to the start of each source file to most effectively state the exclusion of warranty; and each file should have at least the “copyright” line and a pointer to where the full notice is found.
<br>

```
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) <year>  <name of author>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

Also add information on how to contact you by electronic and paper mail.
<br>

If the program does terminal interaction, make it output a short notice like this when it starts in an interactive mode:
<br>

```
    <program>  Copyright (C) <year>  <name of author>
    This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
    This is free software, and you are welcome to redistribute it
    under certain conditions; type `show c' for details.
```

The hypothetical commands \`show w' and `show c' should show the appropriate parts of the General Public License. Of course, your program's commands might be different; for a GUI interface, you would use an “about box”.
<br>

You should also get your employer (if you work as a programmer) or school, if any, to sign a “copyright disclaimer” for the program, if necessary. For more information on this, and how to apply and follow the GNU GPL, see <https://www.gnu.org/licenses/>.
