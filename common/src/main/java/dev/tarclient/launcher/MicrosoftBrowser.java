package dev.tarclient.launcher;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

/** Uses a fresh private browser profile for every new account sign-in. */
public final class MicrosoftBrowser {
    public static boolean isMicrosoftSignIn(String url){
        try{var uri=URI.create(url);return "https".equals(uri.getScheme())&&uri.getUserInfo()==null&&(uri.getPort()==-1||uri.getPort()==443)&&Set.of("microsoft.com","www.microsoft.com","login.microsoftonline.com","login.live.com").contains(uri.getHost());}catch(Exception e){return false;}
    }
    public static boolean openPrivate(String url){
        if(!isMicrosoftSignIn(url))return false;
        for(String variable:List.of("ProgramFiles(x86)","ProgramFiles","LOCALAPPDATA")){
            String root=System.getenv(variable);if(root==null||root.isBlank())continue;
            for(String relative:List.of("Microsoft/Edge/Application/msedge.exe","Google/Chrome/Application/chrome.exe")){
                Path executable=Path.of(root).resolve(relative);if(!Files.isRegularFile(executable))continue;
                Path profile=null;
                try{
                    profile=Files.createTempDirectory("tar-microsoft-signin-").toAbsolutePath().normalize();
                    var process=new ProcessBuilder(executable.toString(),"--user-data-dir="+profile,relative.contains("Edge")?"--inprivate":"--incognito","--no-first-run","--no-default-browser-check","--new-window",url).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                    Path cleanup=profile;Thread.ofVirtual().name("Tar private sign-in cleanup").start(()->{try{process.waitFor();cleanProfile(cleanup);}catch(Exception ignored){}});
                    return true;
                }catch(Exception ignored){if(profile!=null)cleanProfile(profile);}
            }
        }
        return false;
    }
    private static void cleanProfile(Path profile){
        Path temp=Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();
        if(!profile.getParent().equals(temp)||!profile.getFileName().toString().startsWith("tar-microsoft-signin-"))return;
        try(var files=Files.walk(profile)){for(Path file:files.sorted(Comparator.reverseOrder()).toList())try{Files.deleteIfExists(file);}catch(Exception ignored){}}catch(Exception ignored){}
    }
}
