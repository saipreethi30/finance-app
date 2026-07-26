@echo off
cd /d "%~dp0"

if not exist lib\h2-2.2.224.jar (
    echo Downloading libraries...
    powershell -ExecutionPolicy Bypass -File download-libs.ps1
    if errorlevel 1 exit /b 1
)

call compile.bat
