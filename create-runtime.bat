@echo off
set JAVA_HOME=D:\Java\jdk-21.0.6
set JAVAFX_SDK=D:\Java\javafx-sdk-21.0.7

jlink ^
  --module-path "%JAVA_HOME%\jmods;%JAVAFX_SDK%\lib" ^
  --add-modules java.base,java.logging,java.management,java.naming,java.security.jgss,java.instrument,javafx.controls,javafx.fxml,java.sql,jdk.crypto.ec,java.desktop,java.xml,java.scripting ^
  --output build\jre-custom ^
  --compress=2 ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages

rem Copy DLLs to bin\
xcopy /Y /Q "%JAVAFX_SDK%\bin\*.dll" "build\jre-custom\bin\"

rem Copy DLLs to bin\javafx\
mkdir "build\jre-custom\bin\javafx"
xcopy /Y /Q "%JAVAFX_SDK%\bin\*.dll" "build\jre-custom\bin\javafx\"

echo jre-custom created!
pause