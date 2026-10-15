param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Arguments
)

$isSpringBootRun = $false
foreach ($arg in $Arguments) {
    if ($arg -match "spring-boot:run") {
        $isSpringBootRun = $true
        break
    }
}

if ($isSpringBootRun) {
    Write-Host "[news-reader] Dang khoi dong ung dung Web Doc Bao (Design Patterns)..." -ForegroundColor Cyan
    if (-not (Test-Path "target\news-reader-1.0.0.jar")) {
        Write-Host "[news-reader] Chua co file JAR, dang bien dich va dong goi..." -ForegroundColor Yellow
        & "D:\netbeans\java\maven\bin\mvn.cmd" package -DskipTests
    }
    java -jar target\news-reader-1.0.0.jar
} else {
    & "D:\netbeans\java\maven\bin\mvn.cmd" @Arguments
}
