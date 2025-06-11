@echo off
set JAVAFX_SDK=D:\Java\javafx-sdk-21.0.7

jlink ^
  --module-path "%JAVA_HOME%\jmods;%JAVAFX_SDK%\lib" ^
  --add-modules java.base,java.logging,java.management,java.naming,java.security.jgss,java.instrument,javafx.controls,javafx.fxml,java.sql ^
  --output build\jre-custom ^
  --compress 2 ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages

xcopy /E /Y "%JAVAFX_SDK%\bin" "build\jre-custom\bin"

echo jre-custom created!
pause