package dev.tarclient.launcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Per-user installation: copy the complete runtime, then point a desktop shortcut at it. */
public final class DesktopInstaller {
    public record Installation(Path executable,Path shortcut) {}
    public static Installation install() throws Exception {
        String executable=System.getProperty("jpackage.app-path","");
        if(executable.isBlank())throw new IOException("Open the extracted Tar Client.exe to install the desktop launcher.");
        String local=System.getenv("LOCALAPPDATA");
        if(local==null||local.isBlank())throw new IOException("Windows Local AppData was not found.");
        Path source=Path.of(executable).toAbsolutePath().getParent();
        Path installed=copyApplication(source,Path.of(local,"Programs","Tar Client"));
        String script="$ErrorActionPreference='Stop'; $desktop=[Environment]::GetFolderPath('DesktopDirectory'); "
            +"if ([string]::IsNullOrWhiteSpace($desktop)) { throw 'Desktop folder was not found' }; "
            +"$shortcutPath=Join-Path $desktop 'Tar Client.lnk'; "
            +"$link=(New-Object -ComObject WScript.Shell).CreateShortcut($shortcutPath); "
            +"$link.TargetPath="+quote(installed.toString())+"; $link.WorkingDirectory="+quote(installed.getParent().toString())+"; "
            +"$link.IconLocation="+quote(installed+",0")+"; $link.Description='Tar Client - Minecraft 1.21.11'; $link.Save(); "
            +"[Console]::OutputEncoding=[Text.UTF8Encoding]::new(); Write-Output $shortcutPath;";
        Path desktopLink=Path.of(powershell(script).strip());
        if(!Files.isRegularFile(desktopLink))throw new IOException("The app was copied but Windows did not create the desktop shortcut. Installed at: "+installed);
        return new Installation(installed,desktopLink);
    }
    static String quote(String value){return "'"+value.replace("'","''")+"'";}
    static Path copyApplication(Path source,Path installRoot) throws IOException {
        source=source.toRealPath();installRoot=installRoot.toAbsolutePath().normalize();
        if(installRoot.startsWith(source))throw new IOException("The installation folder cannot be inside the source application.");
        for(String required:List.of("Tar Client.exe","app/tar-launcher.jar","app/Tar Client.cfg","runtime/bin/java.exe"))
            if(!Files.isRegularFile(source.resolve(required),LinkOption.NOFOLLOW_LINKS))throw new IOException("Extract the entire download first. Missing "+required);
        // Never overwrite a running installation. Previous versions remain available for rollback.
        Files.createDirectories(installRoot);
        Path target=Files.createTempDirectory(installRoot,TarLauncher.VERSION+"-");
        try(var entries=Files.walk(source)){
            for(Path entry:entries.toList()){
                if(Files.isSymbolicLink(entry))throw new IOException("Application contains a symbolic link; use the original release ZIP.");
                Path to=target.resolve(source.relativize(entry)).normalize();
                if(!to.startsWith(target))throw new IOException("Invalid application path.");
                if(Files.isDirectory(entry,LinkOption.NOFOLLOW_LINKS))Files.createDirectories(to);
                else if(Files.isRegularFile(entry,LinkOption.NOFOLLOW_LINKS))Files.copy(entry,to);
                else throw new IOException("Unsupported application file: "+entry.getFileName());
            }
        }
        return target.resolve("Tar Client.exe");
    }
    private static String powershell(String script) throws Exception {
        Path shell=Path.of(System.getenv("SystemRoot"),"System32","WindowsPowerShell","v1.0","powershell.exe");
        String encoded=Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        var process=new ProcessBuilder(shell.toString(),"-NoLogo","-NoProfile","-NonInteractive","-EncodedCommand",encoded).redirectErrorStream(true).start();
        if(!process.waitFor(Duration.ofSeconds(30).toMillis(),TimeUnit.MILLISECONDS)){process.destroyForcibly();throw new IOException("Windows timed out creating the shortcut.");}
        String result=new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        if(process.exitValue()!=0)throw new IOException("Windows could not create the desktop shortcut. The copied app is still available in Local AppData/Programs/Tar Client.");
        return result;
    }
}
