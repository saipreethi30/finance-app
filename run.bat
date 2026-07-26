@echo off
setlocal

cd /d "%~dp0"

if not exist bin\com\smartfinance\SmartFinanceApp.class (
    echo Project not compiled. Run compile.bat first.
    exit /b 1
)

if "%1"=="" goto menu
if "%1"=="console" goto run_console
if "%1"=="swing" goto run_swing
if "%1"=="javafx" goto run_javafx
echo Unknown option: %1
echo Usage: run.bat [console^|swing^|javafx]
exit /b 1

:menu
java -cp "bin;lib\*" com.smartfinance.SmartFinanceApp
goto end

:run_console
java -cp "bin;lib\*" com.smartfinance.SmartFinanceApp console
goto end

:run_swing
java -cp "bin;lib\*" com.smartfinance.SmartFinanceApp swing
goto end

:run_javafx
java --module-path lib --add-modules javafx.controls,javafx.fxml -cp bin com.smartfinance.SmartFinanceApp javafx
goto end

:end
endlocal
