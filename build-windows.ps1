param([string]$Gradle = 'gradle')
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
& $Gradle --no-daemon :launcher:test :client-mod:build :launcher:fatJar
if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed.' }
$tarJdkBin = Split-Path (Get-Command javac.exe).Source
$tarDist = Join-Path $PSScriptRoot 'dist'
$tarRuntime = Join-Path $tarDist 'runtime'
if (Test-Path $tarRuntime) { throw "Existing runtime at $tarRuntime. Rename the dist folder before rebuilding." }
New-Item -ItemType Directory -Force $tarDist | Out-Null
& (Join-Path $tarJdkBin 'jlink.exe') --add-modules ALL-MODULE-PATH --output $tarRuntime --strip-debug --no-header-files --no-man-pages
if ($LASTEXITCODE -ne 0) { throw 'Runtime creation failed.' }
# Retain native Java commands: GameInstaller launches runtime/bin/java.exe.
& (Join-Path $tarJdkBin 'jpackage.exe') --type app-image --input launcher/build/libs --dest $tarDist --name 'Tar Client' --main-jar tar-launcher.jar --main-class dev.tarclient.launcher.TarLauncher --runtime-image $tarRuntime --app-version 1.0.3 --vendor 'Tarre Industries' --icon branding/tar-client.ico --java-options '-Dfile.encoding=UTF-8'
if ($LASTEXITCODE -ne 0) { throw 'Windows packaging failed.' }
Copy-Item README.md,MODULES.md,SIGN-IN-SETUP.md,WINDOWS-SIGNING.md,CODE-SIGNING.md,PRIVACY.md,CONTRIBUTING.md,LICENSE,THIRD-PARTY.md,TESTING.md,'Start Tar Client.cmd' (Join-Path $tarDist 'Tar Client')
Copy-Item licenses (Join-Path $tarDist 'Tar Client') -Recurse
Compress-Archive -LiteralPath (Join-Path $tarDist 'Tar Client') -DestinationPath (Join-Path $tarDist 'TarClient-1.0.3-Windows.zip')
$tarZip = Join-Path $tarDist 'TarClient-1.0.3-Windows.zip'
$tarHash = Get-FileHash -Algorithm SHA256 -LiteralPath $tarZip
$tarPayloadHash = Join-Path $tarDist 'payload.sha256'
Set-Content -LiteralPath $tarPayloadHash -Value $tarHash.Hash.ToLowerInvariant() -Encoding ascii
$tarBootstrap = Join-Path $tarDist 'TarClient-1.0.3.exe'
$tarCompiler = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
$tarBootstrapSource = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'bootstrap/Program.cs')).Path
$tarBootstrapIcon = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'branding/tar-client.ico')).Path
& $tarCompiler /nologo /target:winexe "/out:$tarBootstrap" /reference:System.IO.Compression.dll /reference:System.IO.Compression.FileSystem.dll /reference:System.Windows.Forms.dll "/win32icon:$tarBootstrapIcon" "/resource:$tarZip,TarPayload.zip" "/resource:$tarPayloadHash,TarPayload.sha256" $tarBootstrapSource
if ($LASTEXITCODE -ne 0) { throw 'Standalone launcher packaging failed.' }
$tarExeHash = Get-FileHash -Algorithm SHA256 -LiteralPath $tarBootstrap
Set-Content -LiteralPath (Join-Path $tarDist 'SHA256SUMS.txt') -Value @(($tarHash.Hash.ToLowerInvariant() + '  TarClient-1.0.3-Windows.zip'), ($tarExeHash.Hash.ToLowerInvariant() + '  TarClient-1.0.3.exe')) -Encoding ascii

