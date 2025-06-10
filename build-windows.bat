@echo off
echo Building .exe with jpackage...

jpackage ^
  --type app-image ^
  --name DataDownloadTool ^
  --input target ^
  --main-jar data-download-tool-0.0.1-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.example.datadownloadtool.MainApp ^
  --runtime-image build\jre-custom ^
  --dest build\dist ^
  --win-console ^
  --java-options "--add-modules=javafx.controls,javafx.fxml -Dprism.order=sw -Dprism.verbose=true"

echo Done! File at: build\dist\DataDownloadTool\DataDownloadTool.exe
pause