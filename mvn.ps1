param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Arguments
)

# Cau hinh moi truong Java neu chua co trong PATH
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Users\HELLO\.jdk\jdk-21.0.10\bin") {
        $env:JAVA_HOME = "C:\Users\HELLO\.jdk\jdk-21.0.10"
        $env:PATH = "C:\Users\HELLO\.jdk\jdk-21.0.10\bin;" + $env:PATH
    }
}

# Tim lenh Maven
$mvnCmd = "mvn"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Users\HELLO\maven\bin\mvn.cmd") {
        $mvnCmd = "C:\Users\HELLO\maven\bin\mvn.cmd"
    } elseif (Test-Path "D:\netbeans\java\maven\bin\mvn.cmd") {
        $mvnCmd = "D:\netbeans\java\maven\bin\mvn.cmd"
    }
}

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
        & $mvnCmd package -DskipTests
    }
    java -jar target\news-reader-1.0.0.jar
} else {
    & $mvnCmd @Arguments
}
