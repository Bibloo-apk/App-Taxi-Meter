@echo off
REM Gradle wrapper script for Windows
if "%JAVA_HOME%"=="" (
  echo JAVA_HOME is not set
)
set DIRNAME=%~dp0
"%DIRNAME%gradlew" %*
