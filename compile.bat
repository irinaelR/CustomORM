@echo off
REM Script to create a JAR file from compiled classes

REM Define paths
set BIN_DIR=bin
set JAR_NAME=customORM.jar

REM Check if the bin directory exists
if not exist "%BIN_DIR%" (
    echo Bin directory not found. Compile your code first.
    exit /b 1
)

REM Create the JAR file
echo Creating JAR file...
jar cvf "%JAR_NAME%" -C "%BIN_DIR%" .

if errorlevel 1 (
    echo Failed to create JAR file.
    exit /b 1
)

echo Build successful. JAR created: %JAR_NAME%
