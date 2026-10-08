package dev.tarclient.launcher;

import java.nio.file.*;
import java.io.IOException;

/** Machine-local choice, intentionally outside module profiles. Missing/corrupt means no sharing. */
public final class PrivacyPreferences {
    private final Path file;
    public PrivacyPreferences(Path folder){file=folder.resolve("badge-sharing.txt");}
    public static PrivacyPreferences defaults(){
        String local=System.getenv("LOCALAPPDATA");
        Path folder=local==null?Path.of(System.getProperty("user.home"),"AppData","Local","TarClient"):Path.of(local,"TarClient");
        return new PrivacyPreferences(Path.of(System.getProperty("tar.data",folder.toString())));
    }
    private String read(){try{return Files.size(file)<32?Files.readString(file).trim():"";}catch(IOException e){return "";}}
    public boolean hasChoice(){return java.util.Set.of("enabled","disabled").contains(read());}
    public boolean sharingEnabled(){return read().equals("enabled");}
    public void save(boolean sharing)throws IOException{
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp=Files.createTempFile(file.toAbsolutePath().getParent(),"privacy-",".tmp");
        try{Files.writeString(temp,sharing?"enabled":"disabled");Net.move(temp,file);}finally{Files.deleteIfExists(temp);}
    }
}
