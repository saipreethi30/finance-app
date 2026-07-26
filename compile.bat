@echo off
setlocal

cd /d "%~dp0"

if not exist lib\h2-2.2.224.jar (
    echo Missing libraries. Run download-libs.ps1 first:
    echo   powershell -ExecutionPolicy Bypass -File download-libs.ps1
    exit /b 1
)

if not exist bin mkdir bin

echo Compiling Java sources...
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "lib\*" @sources.txt
if errorlevel 1 (
    del sources.txt 2>nul
    echo Compile failed.
    exit /b 1
)
del sources.txt

echo Copying resources...
if not exist bin mkdir bin
xcopy /Y /Q src\main\resources\* bin\ >nul

echo.
echo Build successful. Output: bin\
echo Run with: run.bat
endlocal
