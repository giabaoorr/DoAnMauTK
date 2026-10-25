@echo off
setlocal enabledelayedexpansion

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

set "IS_RUN=0"
for %%a in (%*) do (
    if "%%a"=="spring-boot:run" set "IS_RUN=1"
)

if "%IS_RUN%"=="1" (
    echo [news-reader] Dang khoi dong ung dung Web Doc Bao (Design Patterns)...
    if not exist "target\news-reader-1.0.0.jar" (
        echo [news-reader] Chua co file JAR, dang bien dich va dong goi...
        call "%MVN_CMD%" package -DskipTests
    )
    "%JAVA_EXE%" -jar target\news-reader-1.0.0.jar
) else (
    "%MVN_CMD%" %*
)
