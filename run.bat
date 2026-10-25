@echo off
setlocal enabledelayedexpansion
title News Reader - Design Patterns
echo ========================================================
echo   KHOI DONG UNG DUNG DOC BAO (DESIGN PATTERNS)
echo ========================================================

:: 1. Tu dong tim kiem java.exe tren he thong
set "JAVA_EXE="

where java >nul 2>&1
if not errorlevel 1 (
    set "JAVA_EXE=java"
)

if "%JAVA_EXE%"=="" (
    if exist "C:\Users\HELLO\.jdk\jdk-21.0.10\bin\java.exe" (
        set "JAVA_EXE=C:\Users\HELLO\.jdk\jdk-21.0.10\bin\java.exe"
        set "JAVA_HOME=C:\Users\HELLO\.jdk\jdk-21.0.10"
    )
)

if "%JAVA_EXE%"=="" (
    for /d %%D in ("C:\Users\HELLO\.jdk\*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_EXE=%%D\bin\java.exe"
            set "JAVA_HOME=%%D"
        )
    )
)

if "%JAVA_EXE%"=="" (
    for /d %%D in ("C:\Program Files\Java\*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_EXE=%%D\bin\java.exe"
            set "JAVA_HOME=%%D"
        )
    )
)

if "%JAVA_EXE%"=="" (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_EXE=%%D\bin\java.exe"
            set "JAVA_HOME=%%D"
        )
    )
)

if "%JAVA_EXE%"=="" (
    for /d %%D in ("C:\Program Files\Microsoft\*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_EXE=%%D\bin\java.exe"
            set "JAVA_HOME=%%D"
        )
    )
)

if "%JAVA_EXE%"=="" (
    echo [LOI] Khong tim thay Java JDK tren may tinh!
    echo Vui long kiem tra lai thu muc cai dat Java.
    pause
    exit /b 1
)

:: 2. Tu dong tim kiem Maven
set "MVN_CMD="
where mvn >nul 2>&1
if not errorlevel 1 (
    set "MVN_CMD=mvn"
) else if exist "C:\Users\HELLO\maven\bin\mvn.cmd" (
    set "MVN_CMD=C:\Users\HELLO\maven\bin\mvn.cmd"
) else if exist "D:\netbeans\java\maven\bin\mvn.cmd" (
    set "MVN_CMD=D:\netbeans\java\maven\bin\mvn.cmd"
)

:: 3. Kiem tra va bien dich neu chua co file JAR
if not exist "target\news-reader-1.0.0.jar" (
    echo Dang bien dich va dong goi ung dung lan dau...
    if not "%MVN_CMD%"=="" (
        call "%MVN_CMD%" package -DskipTests
    ) else (
        echo [CANH BAO] Khong tim thay Maven de dong goi file JAR.
    )
)

:: 4. Khoi chay ung dung
echo Dang su dung: %JAVA_EXE%
echo Dang khoi chay tren http://localhost:8080 (hoac 8081)...
"%JAVA_EXE%" -jar target\news-reader-1.0.0.jar
pause
