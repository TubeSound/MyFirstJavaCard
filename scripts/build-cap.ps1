#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    throw 'Set JAVA_HOME to a JDK installation.'
}
if ([string]::IsNullOrWhiteSpace($env:JC_HOME_TOOLS)) {
    throw 'Set JC_HOME_TOOLS to Java Card Development Kit Tools 26.0.'
}

$Javac = Join-Path $env:JAVA_HOME 'bin\javac.exe'
$Converter = Join-Path $env:JC_HOME_TOOLS 'bin\converter.bat'
$CardApi = Join-Path $env:JC_HOME_TOOLS 'lib\api_classic-3.0.5.jar'
foreach ($requiredFile in @($Javac, $Converter, $CardApi)) {
    if (!(Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
        throw "Required file missing: $requiredFile"
    }
}

$compilerVersion = & $Javac '-version' 2>&1
if ($LASTEXITCODE -ne 0 -or "$compilerVersion" -notmatch '^javac 25(?:\.|$)') {
    throw "Java Card Tools 26.0 CAP build requires the documented JDK 25 toolchain. Found: $compilerVersion"
}

& (Join-Path $PSScriptRoot 'test.ps1')
if ($LASTEXITCODE -ne 0) {
    throw 'Simulator tests failed; CAP conversion was not started.'
}

$AppletClasses = Join-Path $ProjectRoot 'build\cap-classes'
$OutputDir = Join-Path $ProjectRoot 'build\cap'
foreach ($directory in @($AppletClasses, $OutputDir)) {
    if (Test-Path -LiteralPath $directory) {
        Remove-Item -LiteralPath $directory -Recurse -Force
    }
}
New-Item -ItemType Directory -Force -Path $AppletClasses, $OutputDir | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'src\main\java') `
    -Recurse -File -Filter '*.java' | Sort-Object FullName | ForEach-Object FullName)

& $Javac '--release' '8' '-g' '-encoding' 'UTF-8' '-classpath' $CardApi `
    '-d' $AppletClasses @sources
if ($LASTEXITCODE -ne 0) {
    throw 'Compilation against Java Card 3.0.5 API failed.'
}

$converterArgs = @(
    '-classdir', $AppletClasses,
    '-d', $OutputDir,
    '-target', '3.0.5',
    '-out', 'CAP', 'EXP', 'JCA',
    '-applet', '0xf0:0x54:0x55:0x42:0x45:0x01:0x01',
    'io.github.tubesound.myfirstjavacard.card.PingApplet',
    'io.github.tubesound.myfirstjavacard.card',
    '0xf0:0x54:0x55:0x42:0x45:0x01', '1.0'
)
& $Converter @converterArgs
if ($LASTEXITCODE -ne 0) {
    throw 'CAP conversion or verification failed.'
}

$Cap = Join-Path $OutputDir `
    'io\github\tubesound\myfirstjavacard\card\javacard\card.cap'
if (!(Test-Path -LiteralPath $Cap -PathType Leaf) -or (Get-Item $Cap).Length -eq 0) {
    throw "CAP file was not created: $Cap"
}
Write-Host "Created: $Cap"
