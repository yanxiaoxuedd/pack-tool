@echo off
cd /d "%~dp0"
title MiniPack Builder - KEEP THIS BOX OPEN

echo Folder: %CD%
echo.

if not exist "MiniPack.java" goto NOFILE

set "JC="
set "JR="

rem ---- find any javac ----
for /f "delims=" %%J in ('where javac 2^>nul') do if not defined JC set "JC=%%J"
if not defined JC for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do if exist "%%D\jbr\bin\javac.exe" set "JC=%%D\jbr\bin\javac.exe"
if not defined JC goto NOJAVA

echo Compiler: %JC%

rem ---- prefer a REAL java.exe from IDEA JBR, not the Oracle javapath stub ----
for /f "delims=" %%J in ('where /r "%LOCALAPPDATA%\JetBrains" java.exe 2^>nul') do if not defined JR set "JR=%%J"
if not defined JR for /f "delims=" %%J in ('where /r "%LOCALAPPDATA%\Programs" java.exe 2^>nul') do if not defined JR set "JR=%%J"
if not defined JR for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do if exist "%%D\jbr\bin\java.exe" set "JR=%%D\jbr\bin\java.exe"
if not defined JR for %%J in ("%JC%") do set "JR=%%~dpJjava.exe"

echo Runtime : %JR%
echo.

del MiniPack.class 2>nul
del run.log 2>nul

echo Compiling...
"%JC%" -encoding UTF-8 -source 8 -target 8 MiniPack.java > compile.log 2>&1
type compile.log
if not exist "MiniPack.class" goto FAIL

echo Class OK. Starting GUI NOW (the window is a separate window, not this box).
echo If this box prints [DONE] immediately with no window, read run.log below.
echo.

"%JR%" MiniPack > run.log 2>&1
echo ---- java output ----
type run.log
echo ---------------------
echo.
echo [DONE] Screenshot this whole box if no window appeared.
pause
exit /b 0

:NOFILE
echo [ERROR] MiniPack.java is NOT next to START.bat.
echo Put all extracted files in the SAME folder.
pause
exit /b 1

:NOJAVA
echo [ERROR] javac.exe not found. Use IntelliJ IDEA: File - New - Project - Java,
echo no Maven/Gradle, Location = a BRAND NEW folder (not your mom's project),
echo drag MiniPack.java into src, right click - Run 'MiniPack.main()'.
pause
exit /b 1

:FAIL
echo [ERROR] No MiniPack.class produced. The javac above is a broken stub.
echo compile.log says:
type compile.log
echo.
echo Use IntelliJ IDEA instead: File - New - Project - Java,
echo Location = a BRAND NEW folder, drag MiniPack.java into src, Run.
pause
exit /b 1
