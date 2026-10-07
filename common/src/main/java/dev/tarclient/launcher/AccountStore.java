package dev.tarclient.launcher;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Windows CurrentUser DPAPI; no plaintext credentials or app-wide encryption key. */
public final class AccountStore {
    interface Protector {byte[] apply(byte[] value,boolean encrypt)throws Exception;}
    private final Path folder;
    private final Protector protector;
    public record Saved(List<MicrosoftAuth.Session> accounts,String selected){}
    public AccountStore(Path folder){this(folder,AccountStore::windowsProtect);}
    AccountStore(Path folder,Protector protector){this.folder=folder;this.protector=protector;}
    public static AccountStore defaults(){return new AccountStore(Path.of(System.getProperty("tar.data",Path.of(System.getProperty("user.home"),"AppData","Local","TarClient").toString())));}
    public Saved read()throws Exception {return locked(()->readUnlocked());}
    private Saved readUnlocked()throws Exception {
        Path path=folder.resolve("accounts.dpapi");if(!Files.exists(path))return new Saved(List.of(),"");
        if(Files.size(path)>1024*1024)throw new IOException("Saved account file is too large.");
        byte[] plain=protector.apply(Files.readAllBytes(path),false);
        try{var data=JsonParser.parseString(new String(plain,StandardCharsets.UTF_8)).getAsJsonObject();var accounts=new ArrayList<MicrosoftAuth.Session>();
            for(var item:data.getAsJsonArray("accounts")){var account=Net.JSON.fromJson(item,MicrosoftAuth.Session.class);if(!account.demo()&&account.uuid().matches("[0-9a-fA-F]{32}")&&accounts.size()<32)accounts.add(account);}
            return new Saved(List.copyOf(accounts),data.has("selected")?data.get("selected").getAsString():"");
        }finally{Arrays.fill(plain,(byte)0);}
    }
    public void remember(MicrosoftAuth.Session session,boolean select)throws Exception {
        if(session.demo()||session.refreshToken().isBlank())return;
        locked(()->{var old=readUnlocked();var accounts=new LinkedHashMap<String,MicrosoftAuth.Session>();for(var entry:old.accounts())accounts.put(entry.uuid(),entry);if(!accounts.containsKey(session.uuid())&&accounts.size()>=32)throw new IOException("Forget an account before saving more than 32 accounts.");accounts.put(session.uuid(),session);write(new Saved(List.copyOf(accounts.values()),select?session.uuid():old.selected()));return null;});
    }
    public void forget(String uuid)throws Exception {locked(()->{var old=readUnlocked();write(new Saved(old.accounts().stream().filter(s->!s.uuid().equals(uuid)).toList(),old.selected().equals(uuid)?"":old.selected()));return null;});}
    public void deselect()throws Exception {locked(()->{var old=readUnlocked();write(new Saved(old.accounts(),""));return null;});}
    private void write(Saved saved)throws Exception {
        byte[] plain=Net.JSON.toJson(saved).getBytes(StandardCharsets.UTF_8),encrypted;
        try{encrypted=protector.apply(plain,true);}finally{Arrays.fill(plain,(byte)0);}
        Path temp=Files.createTempFile(folder,"account-",".dpapi");try{Files.write(temp,encrypted);Net.move(temp,folder.resolve("accounts.dpapi"));}finally{Files.deleteIfExists(temp);}
    }
    private synchronized <T>T locked(Callable<T> action)throws Exception {Files.createDirectories(folder);try(var channel=java.nio.channels.FileChannel.open(folder.resolve("accounts.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var lock=channel.lock()){return action.call();}}
    private static byte[] windowsProtect(byte[] input,boolean encrypt)throws Exception {
        String root=System.getenv("SystemRoot");if(root==null)throw new IOException("Remembered sign-in requires Windows encryption.");
        String script="$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.Security; $bytes=[Convert]::FromBase64String([Console]::ReadLine()); $out=[Security.Cryptography.ProtectedData]::"+(encrypt?"Protect":"Unprotect")+"($bytes,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser); [Console]::Write([Convert]::ToBase64String($out));";
        var process=new ProcessBuilder(Path.of(root,"System32","WindowsPowerShell","v1.0","powershell.exe").toString(),"-NoLogo","-NoProfile","-NonInteractive","-EncodedCommand",Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE))).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        var result=new FutureTask<byte[]>(()->process.getInputStream().readNBytes(2*1024*1024));Thread.ofVirtual().start(result);
        try{
            try(var out=process.getOutputStream()){out.write(Base64.getEncoder().encode(input));out.write('\n');}
            if(!process.waitFor(30,TimeUnit.SECONDS)){process.destroyForcibly();throw new IOException("Windows account encryption timed out.");}
            if(process.exitValue()!=0)throw new IOException("Windows could not unlock saved accounts. Sign in again on this Windows user.");
            return Base64.getDecoder().decode(new String(result.get(5,TimeUnit.SECONDS),StandardCharsets.US_ASCII).trim());
        }finally{process.destroy();result.cancel(true);}
    }
}
