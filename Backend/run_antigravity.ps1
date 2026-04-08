# Script para correr la aplicación Backend en Antigravity
# Este script establece la variable JAVA_HOME necesaria para usar el JDK de IntelliJ

$JdkPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.4\jbr"
$env:JAVA_HOME = $JdkPath
$env:Path = "$JdkPath\bin;" + $env:Path

Write-Host "Iniciando Backend en Antigravity..." -ForegroundColor Cyan
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev" "-DskipTests"
