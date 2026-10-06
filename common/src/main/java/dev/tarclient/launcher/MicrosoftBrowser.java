package dev.tarclient.launcher;

import java.net.URI;
import java.nio.file.*;
import java.util.*;

/** Opens a separate private sign-in window without clearing the user's browser cookies. */
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
                try{new ProcessBuilder(executable.toString(),relative.contains("Edge")?"--inprivate":"--incognito","--new-window",url).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();return true;}catch(Exception ignored){}
            }
        }
        return false;
    }
}
