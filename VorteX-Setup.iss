[Setup]
AppName=VorteX Wizard
AppVersion=1.0.0
AppVerName=VorteX Wizard {#SetupSetting('AppVersion')}
DefaultDirName={commonpf32}\VorteX
DefaultGroupName=VorteX
OutputDir="D:\VorteX-Builds"
SourceDir={#SetupSetting('OutputDir')}
OutputBaseFilename=VorteX-Setup-{#SetupSetting('AppVersion')}
ChangesAssociations=yes

[InstallDelete]
Type: filesandordirs; Name: "{app}\update"

[Dirs]
Name: "{app}"
Name: "{app}\temp"; Flags: uninsneveruninstall

[Files]
Source: "LICENSE"; DestDir: "{app}"; Flags: ignoreversion
Source: "vxar.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "settings.json"; DestDir: "{app}"; Flags: uninsneveruninstall
Source: "installation-wizard.bmp"; DestDir: "{tmp}"; Flags: dontcopy

[Icons]
Name: "{commondesktop}\VorteX"; Filename: "{app}\vxar.exe"; IconFilename: "{app}\icon.ico"; Tasks: desktopicon; Check: ShouldCreateDesktopShortcut
Name: "{group}\VorteX"; Filename: "{app}\vxar.exe"; IconFilename: "{app}\icon.ico"

[Registry]
Root: HKLM; Subkey: "SOFTWARE\VorteX"; ValueType: string; ValueName: "VorteX_HOME"; ValueData: "{app}"; Flags: uninsdeletevalue
Root: HKLM; Subkey: "SOFTWARE\VorteX"; ValueType: string; ValueName: "VorteX_Version"; ValueData: "{#SetupSetting('AppVersion')}"; Flags: uninsdeletevalue
Root: HKCR; Subkey: ".vxar"; ValueType: string; ValueData: "VorteXArchive"; Flags: uninsdeletekey; Tasks: defaultapp; Check: ShouldSetDefaultApp
Root: HKCR; Subkey: "VorteXArchive"; ValueType: string; ValueData: "VorteX Archive"; Tasks: defaultapp; Check: ShouldSetDefaultApp
Root: HKCR; Subkey: "VorteXArchive\DefaultIcon"; ValueType: string; ValueData: "{app}\vxar.exe,0"; Tasks: defaultapp; Check: ShouldSetDefaultApp
Root: HKCR; Subkey: "VorteXArchive\shell\open\command"; ValueType: string; ValueData: """{app}\vxar.exe"" ""%1"""; Tasks: defaultapp; Check: ShouldSetDefaultApp

[Run]
Filename: "{sys}\WindowsPowerShell\v1.0\powershell.exe"; \
Parameters: "Start-Process PowerShell -Verb RunAs -ArgumentList 'Add-MpPreference -ExclusionPath ''{app}'' -ExclusionProcess ''{app}\vxar.exe'' -ExclusionExtension ''.vxar'''"; \
WorkingDir: "{sys}"; Tasks: windowsdefenderexclusion; Check: ShouldAddWindowsDefenderExclusion
Filename: "{app}\vxar.exe"; Tasks: openvortex; Check: ShouldOpenVorteX

[Code]
var
  CustomPage: TWizardPage;
  Image: TBitmapImage;
  BitmapFilePath: String;
  ShouldCreateShortcut: Boolean;
  ShouldSetDefaultAppForVXARFiles: Boolean;
  ShouldAddWindowsDefenderExclusionForApplication: Boolean;
  ShouldOpenVorteXApplication: Boolean;

function InitializeSetup(): Boolean;
begin
  ShouldCreateShortcut := True;
  ShouldSetDefaultAppForVXARFiles := True;
  ShouldAddWindowsDefenderExclusionForApplication := True;
  ShouldOpenVorteXApplication := True;
  Result := True;
end;

procedure InitializeWizard;
begin
  CustomPage := CreateCustomPage(wpWelcome, 'Installation wizard', 'Let the installation wizard install this software for you...');

  Image := TBitmapImage.Create(WizardForm);
  Image.Parent := CustomPage.Surface;

  try
    ExtractTemporaryFile('installation-wizard.bmp');
    BitmapFilePath := ExpandConstant('{tmp}\installation-wizard.bmp');
   
    Image.Bitmap.LoadFromFile(BitmapFilePath);
    Image.Width := Image.Bitmap.Width;
    Image.Height := Image.Bitmap.Height;

    Image.Left := (CustomPage.SurfaceWidth - Image.Width) div 2;
    Image.Top := (CustomPage.SurfaceHeight - Image.Height) div 2;
  except
    MsgBox('Error loading installation-wizard.bmp', mbError, MB_OK);
  end;
end;

procedure CurPageChanged(CurPageID: Integer);
begin
  if CurPageID = wpSelectTasks then
  begin
    WizardForm.TasksList.Checked[0] := ShouldCreateShortcut;
    WizardForm.TasksList.Checked[1] := ShouldSetDefaultAppForVXARFiles;
    WizardForm.TasksList.Checked[2] := ShouldAddWindowsDefenderExclusionForApplication;
    WizardForm.TasksList.Checked[3] := ShouldOpenVorteXApplication;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    ShouldCreateShortcut := WizardForm.TasksList.Checked[0];
    ShouldSetDefaultAppForVXARFiles := WizardForm.TasksList.Checked[1];
    ShouldAddWindowsDefenderExclusionForApplication := WizardForm.TasksList.Checked[2];
    ShouldOpenVorteXApplication := WizardForm.TasksList.Checked[3];
  end;
end;

function ShouldCreateDesktopShortcut(): Boolean;
begin
  Result := ShouldCreateShortcut;
end;

function ShouldSetDefaultApp(): Boolean;
begin
  Result := ShouldSetDefaultAppForVXARFiles;
end;

function ShouldAddWindowsDefenderExclusion(): Boolean;
begin
  Result := ShouldAddWindowsDefenderExclusionForApplication;
end;

function ShouldOpenVorteX(): Boolean;
begin
  Result := ShouldOpenVorteXApplication;
end;

[Tasks]
Name: "desktopicon"; Description: "Create a desktop icon"; Flags: unchecked
Name: "defaultapp"; Description: "Set 'VorteX' as default application for '.vxar' files"; Flags: unchecked
Name: "windowsdefenderexclusion"; Description: "Exclude directory '{app}', process '{app}\vxar.exe' and '.vxar' file extension from Windows Defender. Read here (https://github.com/BlockyDotJar/VorteX#windows-defender) why this might be needed."; Flags: unchecked
Name: "openvortex"; Description: "Open VorteX after installation"; Flags: unchecked
