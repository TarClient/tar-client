$ErrorActionPreference = 'Stop'
$tarExe = (Resolve-Path -LiteralPath './dist/TarClient-1.1.4.exe').Path
$tarShell = Join-Path $env:SystemRoot 'System32/WindowsPowerShell/v1.0/powershell.exe'
$tarParent = Start-Process -FilePath $tarShell -ArgumentList '-NoProfile','-NonInteractive','-Command','Start-Sleep -Seconds 4' -WindowStyle Hidden -PassThru
$tarUpdate = Start-Process -FilePath $tarExe -ArgumentList '--install-only','--update-from',($tarParent.Id.ToString()) -WindowStyle Hidden -PassThru
if (-not $tarUpdate.WaitForExit(120000)) { $tarUpdate.Kill(); throw 'Update handoff timed out' }
if ($tarUpdate.ExitCode -ne 0 -or -not $tarParent.HasExited) { throw 'Update did not wait for the previous launcher' }
$tarInvalid = Start-Process -FilePath $tarExe -ArgumentList '--install-only','--update-from','-1' -WindowStyle Hidden -PassThru
if (-not $tarInvalid.WaitForExit(30000)) { $tarInvalid.Kill(); throw 'Invalid handoff did not exit' }
if ($tarInvalid.ExitCode -eq 0) { throw 'Invalid parent PID was accepted' }

$tarRoot = Join-Path $env:LOCALAPPDATA 'Programs/Tar Client'
$tarPointer = Join-Path $tarRoot 'current-install.txt'
$tarSaved = [IO.File]::ReadAllText($tarPointer)
$tarAssembly = [Reflection.Assembly]::LoadFile($tarExe)
$tarPreferred = $tarAssembly.GetType('Program').GetMethod('PreferredInstallation',[Reflection.BindingFlags]'Static,NonPublic')
$tarExpected = Join-Path (Join-Path $tarRoot $tarSaved) 'Tar Client/Tar Client.exe'
if ($tarPreferred.Invoke($null,@()) -ne $tarExpected) { throw 'Installed version was not remembered' }
try {
    [IO.File]::WriteAllText($tarPointer,'../../outside.exe')
    if ($null -ne $tarPreferred.Invoke($null,@())) { throw 'Pointer traversal was accepted' }
    # Only validate selection; never launch this synthetic future-version fixture.
    $tarHash = 'a' * 64
    $tarName = '9.0.0-' + $tarHash.Substring(0,12)
    $tarFuture = Join-Path $tarRoot $tarName
    foreach ($tarFile in @('Tar Client.exe','app/tar-launcher.jar','app/Tar Client.cfg','runtime/bin/java.exe','runtime/lib/modules')) {
        $tarFixture = Join-Path (Join-Path $tarFuture 'Tar Client') $tarFile
        New-Item -ItemType Directory -Force -Path (Split-Path $tarFixture) | Out-Null
        [IO.File]::WriteAllText($tarFixture,'Synthetic selection fixture, never executed')
    }
    [IO.File]::WriteAllText((Join-Path $tarFuture '.complete'),$tarHash)
    [IO.File]::WriteAllText($tarPointer,$tarName)
    if ($tarPreferred.Invoke($null,@()) -ne (Join-Path $tarFuture 'Tar Client/Tar Client.exe')) { throw 'Old desktop EXE would not select newer installed version' }
    [IO.File]::WriteAllText((Join-Path $tarFuture '.complete'),'incomplete')
    if ($null -ne $tarPreferred.Invoke($null,@())) { throw 'Incomplete installation was selected' }
} finally { [IO.File]::WriteAllText($tarPointer,$tarSaved) }
Write-Output 'Update handoff, invalid PID, version pointer, traversal rejection and incomplete-install fallback passed.'
