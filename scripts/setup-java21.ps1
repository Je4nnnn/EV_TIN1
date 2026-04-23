$ErrorActionPreference = 'Stop'

$RootDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$ToolsDir = Join-Path $RootDir 'tools'
$JdkDir = Join-Path $ToolsDir 'jdk-21'
$ArchivePath = Join-Path $ToolsDir 'jdk-21.zip'
$DownloadUrl = if ($env:JDK21_DOWNLOAD_URL) {
    $env:JDK21_DOWNLOAD_URL
} else {
    'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse'
}

if (Test-Path (Join-Path $JdkDir 'bin\javac.exe')) {
    Write-Host "Java 21 JDK already available at $JdkDir"
    exit 0
}

New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null

Write-Host "Downloading Java 21 JDK from $DownloadUrl"
Invoke-WebRequest -Uri $DownloadUrl -OutFile $ArchivePath

if (Test-Path $JdkDir) {
    Remove-Item -LiteralPath $JdkDir -Recurse -Force
}

Expand-Archive -LiteralPath $ArchivePath -DestinationPath $ToolsDir -Force

$ExtractedDir = Get-ChildItem -Path $ToolsDir -Directory | Where-Object {
    $_.Name -like 'jdk-*' -and $_.Name -ne 'jdk-21'
} | Select-Object -First 1

if (-not $ExtractedDir) {
    throw 'Could not locate extracted JDK directory.'
}

Move-Item -LiteralPath $ExtractedDir.FullName -Destination $JdkDir

Write-Host "Java 21 JDK installed at $JdkDir"
