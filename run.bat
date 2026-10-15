@echo off
title News Reader - Design Patterns
echo ========================================================
echo   KHOI DONG UNG DUNG DOC BAO (DESIGN PATTERNS)
echo ========================================================
if not exist "target\news-reader-1.0.0.jar" (
    echo Dang bien dich va dong goi ung dung lan dau...
    call "D:\netbeans\java\maven\bin\mvn.cmd" package -DskipTests
)
echo Dang khoi chay tren http://localhost:8080 (hoac 8081)...
java -jar target\news-reader-1.0.0.jar
pause
