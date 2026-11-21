@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo ERRO: JDK nao encontrado. Instale JDK 17+ e configure o PATH.
  exit /b 1
)
if not exist build mkdir build
set "LISTA=%TEMP%\farma-aura-sprint4-fontes-%RANDOM%.txt"
if exist "!LISTA!" del "!LISTA!"
for %%F in (src\*.java Gui\*.java verificacao\*.java) do echo %%F>> "!LISTA!"
javac -encoding UTF-8 -d build @"!LISTA!"
set "RESULTADO=!errorlevel!"
del "!LISTA!"
if not "!RESULTADO!"=="0" exit /b !RESULTADO!
echo Codigo Sprint 4 compilado.
exit /b 0
