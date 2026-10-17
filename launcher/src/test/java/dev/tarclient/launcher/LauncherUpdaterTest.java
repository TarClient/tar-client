package dev.tarclient.launcher;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

class LauncherUpdaterTest {
    @TempDir Path dir;
    final byte[] payload="test fixture, never executed".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    String digest() throws Exception {return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(payload));}
    JsonObject release(String version) throws Exception {
        var r=new JsonObject();r.addProperty("tag_name","v"+version);r.addProperty("draft",false);r.addProperty("prerelease",false);
        var a=new JsonObject();a.addProperty("name","TarClient-"+version+".exe");a.addProperty("browser_download_url","https://github.com/TarClient/tar-client/releases/download/v"+version+"/TarClient-"+version+".exe");a.addProperty("size",payload.length);a.addProperty("digest","sha256:"+digest());a.addProperty("state","uploaded");var assets=new JsonArray();assets.add(a);r.add("assets",assets);return r;
    }
    JsonObject asset(JsonObject r){return r.getAsJsonArray("assets").get(0).getAsJsonObject();}
    @Test void comparesNumericVersionsAndRejectsMalformedTags() throws Exception {
        assertTrue(LauncherUpdater.compare("1.10.0","1.9.9")>0);assertTrue(LauncherUpdater.compare("2.0.0","1.99.99")>0);assertEquals(0,LauncherUpdater.compare("1.1.2","1.1.2"));
        for(String v:List.of("1.2","1.2.3-preview","../1.2.3","1.2.999999999999","v1.2.3"))assertThrows(IOException.class,()->LauncherUpdater.compare(v,"1.0.0"));
    }
    @Test void neverDowngradesOrReinstallsSameVersion() throws Exception {
        assertNull(LauncherUpdater.select(release("1.0.0").toString(),"1.1.2"));assertNull(LauncherUpdater.select(release("1.1.2").toString(),"1.1.2"));assertEquals("1.2.0",LauncherUpdater.select(release("1.2.0").toString(),"1.1.2").version());
    }
    @Test void excludesDraftAndPreviewReleases() throws Exception {
        for(String flag:List.of("draft","prerelease")){var r=release("9.0.0");r.addProperty(flag,true);assertNull(LauncherUpdater.select(r.toString(),"1.1.2"));}
    }
    @Test void requiresExactRepositoryAndVersionAssetUrl() throws Exception {
        for(String url:List.of("http://github.com/TarClient/tar-client/releases/download/v2.0.0/TarClient-2.0.0.exe","https://evil.example/update.exe","https://github.com/Other/tar-client/releases/download/v2.0.0/TarClient-2.0.0.exe","https://github.com/TarClient/tar-client/releases/download/v1.0.0/TarClient-1.0.0.exe","https://github.com@evil.example/update.exe")){
            var r=release("2.0.0");asset(r).addProperty("browser_download_url",url);assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));
        }
    }
    @Test void rejectsMissingChecksumsAndOversizedAssets() throws Exception {
        var r=release("2.0.0");asset(r).remove("digest");assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));
        asset(r).addProperty("digest","sha256:bad");assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));
        asset(r).addProperty("digest","sha256:"+digest());asset(r).addProperty("size",LauncherUpdater.MAX_BYTES+1);assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));
    }
    @Test void rejectsAmbiguousIncompleteAndMalformedReleases() throws Exception {
        var r=release("2.0.0");r.getAsJsonArray("assets").add(asset(r).deepCopy());assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));
        r.getAsJsonArray("assets").remove(1);asset(r).addProperty("state","new");assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));
        r.add("assets",new JsonArray());assertThrows(IOException.class,()->LauncherUpdater.select(r.toString(),"1.1.2"));assertThrows(IOException.class,()->LauncherUpdater.select("{}","1.1.2"));
    }
    LauncherUpdater updater(String metadata,byte[] bytes,boolean fail){return new LauncherUpdater(new LauncherUpdater.Transport(){
        public String latest(){return metadata;}
        public void download(LauncherUpdater.Update u,Path p,Consumer<String> progress) throws Exception {Files.write(p,bytes);if(fail)throw new IOException("Disconnected");}
    });}
    @Test void verifiedPackageIsStagedWithoutChangingAccountsOrInstallation() throws Exception {
        Path account=dir.resolve("accounts.dpapi"),settings=dir.resolve("launcher.json");Files.writeString(account,"protected-account-fixture");Files.writeString(settings,"{\"autoUpdate\":true}");
        Path downloaded=updater(release("2.0.0").toString(),payload,false).prepare("1.1.2",dir,s->{});
        assertTrue(downloaded.startsWith(dir.resolve("updates")));assertArrayEquals(payload,Files.readAllBytes(downloaded));assertEquals("protected-account-fixture",Files.readString(account));assertEquals("{\"autoUpdate\":true}",Files.readString(settings));
        if(System.getProperty("os.name").startsWith("Windows")){
            var attrs=Files.getFileAttributeView(downloaded,java.nio.file.attribute.UserDefinedFileAttributeView.class);var zone=java.nio.ByteBuffer.allocate(attrs.size("Zone.Identifier"));attrs.read("Zone.Identifier",zone);zone.flip();assertTrue(java.nio.charset.StandardCharsets.UTF_8.decode(zone).toString().contains("ZoneId=3"));
        }
    }
    @Test void tamperingIsRejectedAndNeverLeavesExecutable() throws Exception {
        byte[] corrupt=payload.clone();corrupt[0]^=1;
        assertThrows(IOException.class,()->updater(release("2.0.0").toString(),corrupt,false).prepare("1.1.2",dir,s->{}));assertNoExecutable();
    }
    @Test void truncatedOrInterruptedDownloadIsDiscarded() throws Exception {
        assertThrows(IOException.class,()->updater(release("2.0.0").toString(),new byte[2],false).prepare("1.1.2",dir,s->{}));
        assertThrows(IOException.class,()->updater(release("2.0.0").toString(),payload,true).prepare("1.1.2",dir,s->{}));assertNoExecutable();
    }
    @Test void noStableReleaseDoesNotCreateUpdateFolder() throws Exception {
        assertNull(updater(null,payload,false).prepare("1.1.2",dir,s->{}));assertNull(updater(release("1.0.0").toString(),payload,false).prepare("1.1.2",dir,s->{}));assertFalse(Files.exists(dir.resolve("updates")));
    }
    void assertNoExecutable() throws Exception {try(var files=Files.walk(dir)){assertFalse(files.anyMatch(p->p.toString().endsWith(".exe")));}}
}
