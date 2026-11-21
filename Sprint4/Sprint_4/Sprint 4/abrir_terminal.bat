@echo off
cd /d "%~dp0"
call "compilar.bat"
if errorlevel 1 (pause & exit /b 1)
java -cp build Main
pause
