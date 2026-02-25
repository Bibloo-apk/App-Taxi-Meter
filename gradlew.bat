@echo off
REM Gradle wrapper script for Windows

set DIRNAME=%~dp0
if exist "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" (
    java -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
) else (
    echo ERROR: gradle-wrapper.jar not found. Please make sure you uploaded "gradle\wrapper\gradle-wrapper.jar"
    exit /b 1
)