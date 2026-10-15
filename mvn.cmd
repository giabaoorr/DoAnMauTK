@echo off
setlocal
set "IS_RUN=0"
for %%a in (%*) do (
    if "%%a"=="spring-boot:run" set "IS_RUN=1"
)
if "%IS_RUN%"=="1" (
    echo [news-reader] Dang khoi dong ung dung Web Doc Bao (Design Patterns)...
    if not exist "target\news-reader-1.0.0.jar" (
        echo [news-reader] Chua co file JAR, dang bien dich va dong goi...
        call "D:\netbeans\java\maven\bin\mvn.cmd" package -DskipTests
    )
    java -jar target\news-reader-1.0.0.jar
) else (
    "D:\netbeans\java\maven\bin\mvn.cmd" %*
)
