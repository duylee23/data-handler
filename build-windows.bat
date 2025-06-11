@echo off
echo Building .exe with jpackage...

jpackage ^
  --type exe ^
  --name DataDownloadTool ^
  --input target ^
  --main-jar data-download-tool-0.0.1-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.example.datadownloadtool.MainApp ^
  --runtime-image build\jre-custom ^
  --dest build\dist ^
  --win-console ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser ^
  --app-version 1.0 ^
  --vendor "INFINIQ" ^
  --description "Tool to manage data" ^
  --java-options "--add-modules=javafx.controls" ^
  --java-options "--add-modules=javafx.fxml" ^
  --java-options "-Dprism.order=sw" ^
  --java-options "-Dprism.verbose=true"

echo Done!
pause