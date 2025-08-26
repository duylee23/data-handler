@echo off
echo Building .msi with jpackage...

jpackage ^
  --type msi  ^
  --name DataDownloadTool ^
  --input target ^
  --main-jar data-download-tool-0.0.1-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.example.datadownloadtool.MainApp ^
  --runtime-image build\jre-custom ^
  --dest build\dist ^
  --app-version 1.0 ^
  --verbose ^
  --win-console ^
  --win-shortcut ^
  --win-dir-chooser ^
  --vendor "INFINIQ" ^
  --description "Tool to manage data" ^
  --java-options "--add-modules=javafx.controls,javafx.fxml --show-version --show-module-resolution -Dprism.verbose=true -Djavafx.verbose=true"

echo MSI created successfully!
pause