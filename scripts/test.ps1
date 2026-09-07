#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
& (Join-Path $PSScriptRoot 'build.ps1')
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (![string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    $Java = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    $Java = (Get-Command 'java.exe' -ErrorAction Stop).Source
}
$separator = [IO.Path]::PathSeparator
$classPath = (Join-Path $ProjectRoot 'build\classes') + $separator `
    + (Join-Path $ProjectRoot 'lib\jcardsim-3.0.6.0.jar')

& $Java '-ea' '-cp' $classPath 'io.github.tubesound.myfirstjavacard.card.TestRunner'
exit $LASTEXITCODE
