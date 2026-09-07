#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$Jar = Join-Path $ProjectRoot 'lib\jcardsim-3.0.6.0.jar'
$Classes = Join-Path $ProjectRoot 'build\classes'
$ExpectedSha256 = 'db6de7ffde71651c45d00df7e160771685cec3e14e444bf36b796658d289942f'

function Find-JdkCommand([string] $Name) {
    if (![string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $candidate = Join-Path $env:JAVA_HOME "bin\$Name.exe"
        if (Test-Path -LiteralPath $candidate -PathType Leaf) {
            return $candidate
        }
    }
    $command = Get-Command "$Name.exe" -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        throw "$Name.exe was not found. Install a JDK and set JAVA_HOME."
    }
    return $command.Source
}

if (!(Test-Path -LiteralPath $Jar -PathType Leaf)) {
    throw "Missing dependency: $Jar"
}
$actualSha256 = (Get-FileHash -LiteralPath $Jar -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualSha256 -ne $ExpectedSha256) {
    throw "jCardSim SHA-256 mismatch. Expected $ExpectedSha256 but found $actualSha256"
}

$Javac = Find-JdkCommand 'javac'
$version = & $Javac '-version' 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "javac failed: $version"
}
Write-Host "Compiler: $version"

if (Test-Path -LiteralPath $Classes) {
    Remove-Item -LiteralPath $Classes -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $Classes | Out-Null

$mainSources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'src\main\java') `
    -Recurse -File -Filter '*.java' | Sort-Object FullName | ForEach-Object FullName)
$testSources = @(Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'src\test\java') `
    -Recurse -File -Filter '*.java' | Sort-Object FullName | ForEach-Object FullName)
if ($mainSources.Count -eq 0 -or $testSources.Count -eq 0) {
    throw 'Java source files were not found.'
}

& $Javac '--release' '8' '-g' '-encoding' 'UTF-8' '-classpath' $Jar `
    '-d' $Classes @mainSources
if ($LASTEXITCODE -ne 0) {
    throw 'Applet compilation failed.'
}

$separator = [IO.Path]::PathSeparator
$testClassPath = "$Classes$separator$Jar"
& $Javac '--release' '8' '-g' '-encoding' 'UTF-8' '-classpath' $testClassPath `
    '-d' $Classes @testSources
if ($LASTEXITCODE -ne 0) {
    throw 'Test compilation failed.'
}

Write-Host "Build completed: $Classes"
