package dev.tarclient.launcher;

import com.google.gson.*;
import dev.tarclient.config.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;

class Update041Test {
    @TempDir Path dir;
    @Test void privateSignInOnlyAcceptsMicrosoftHttpsPages(){
        assertTrue(MicrosoftBrowser.isMicrosoftSignIn("https://microsoft.com/devicelogin"));
        for(String url:List.of("http://microsoft.com/","https://microsoft.com.evil.test/","https://user@microsoft.com/","file:///C:/test","https://microsoft.com:1234/"))assertFalse(MicrosoftBrowser.isMicrosoftSignIn(url));
    }
    @Test void gridDesignSurvivesProfilesAndInvalidDataIsSafe()throws Exception{
        var c=new ClientConfig();String drawing=CrosshairPattern.paint("0".repeat(225),0,14,true);
        c.set("crosshair","pattern",drawing);c.set("crosshair","grid",true);c.set("crosshair","vanilla",false);
        var profiles=new ProfileStore(dir);profiles.create("Hand drawn",c);var restored=profiles.load("Hand drawn");
        assertEquals(drawing,restored.text("crosshair","pattern"));assertTrue(restored.bool("crosshair","grid"));assertFalse(restored.bool("crosshair","vanilla"));
        assertTrue(CrosshairPattern.pixel(drawing,0,14));assertFalse(CrosshairPattern.pixel(drawing,14,0));
        assertEquals(drawing,CrosshairPattern.paint(drawing,-1,0,true));assertEquals(drawing,CrosshairPattern.paint(drawing,15,0,true));
        assertEquals(CrosshairPattern.vanilla(),CrosshairPattern.normalize("corrupt"));
        assertEquals("0".repeat(225),CrosshairPattern.paint(drawing,0,14,false));
    }
    @Test void oldConfigurationsLose3dSkinsAndGetNativeCrosshair()throws Exception{
        Path file=dir.resolve("settings.json");Files.writeString(file,"{\"skins3d\":{\"enabled\":true},\"crosshair\":{\"size\":9},\"armor\":{\"reverse\":true}}");
        var c=ClientConfig.read(file);assertTrue(c.bool("crosshair","vanilla"));assertEquals(9,c.i("crosshair","size"));assertTrue(c.bool("armor","reverse"));
        c.save(file);assertFalse(Files.readString(file).contains("skins3d"));assertNull(Integrations.find("skins3d"));
        assertTrue(c.bool("potions","duration"));assertTrue(c.bool("potions","showBackground"));
    }
    @Test void removingSkinLayersPreservesExactJarAndOtherMods()throws Exception{
        var manager=new ModManager(dir,s->{});Path skin=jar("skinlayers.jar","skinlayers3d","1.0"),other=jar("other.jar","example","1.0");
        manager.importJar(skin);manager.importJar(other);byte[] bytes=Files.readAllBytes(skin);
        manager.removeRetiredIntegrations();manager.removeRetiredIntegrations();assertEquals(1,manager.list().size());assertEquals("example",manager.metadata(manager.list().getFirst()).get("id").getAsString());
        try(var backups=Files.list(dir.resolve("removed-mods"))){assertArrayEquals(bytes,Files.readAllBytes(backups.findFirst().orElseThrow()));}
    }
    @Test void patchBacksUp040CoreAndKeepsWorld()throws Exception{
        Files.createDirectories(dir.resolve("mods"));Files.copy(jar("old.jar","tarclient","0.4.0"),dir.resolve("mods/tar-client-0.4.0.jar"));
        Path world=dir.resolve("saves/My World/level.dat");Files.createDirectories(world.getParent());Files.writeString(world,"world");
        byte[] update=Files.readAllBytes(jar("new.jar","tarclient","0.4.1"));CoreInstaller.install(dir,new ByteArrayInputStream(update),"0.4.1");
        assertFalse(Files.exists(dir.resolve("mods/tar-client-0.4.0.jar")));assertEquals("world",Files.readString(world));assertArrayEquals(update,Files.readAllBytes(dir.resolve("mods/tar-client-0.4.1.jar")));
    }
    static class FakeAuth implements MicrosoftAuth.Transport {
        int polls;String name="SecondAccount";boolean owns=true,failXbox;
        static JsonObject json(String value){return JsonParser.parseString(value).getAsJsonObject();}
        public JsonObject form(String url,Map<String,String> values){
            assertEquals(MicrosoftAuth.DEFAULT_CLIENT_ID,values.get("client_id"));
            if(url.endsWith("devicecode"))return json("{\"expires_in\":900,\"interval\":1,\"user_code\":\"USERCODE\",\"device_code\":\"PRIVATECODE\",\"verification_uri\":\"https://microsoft.com/devicelogin\"}");
            assertEquals("PRIVATECODE",values.get("device_code"));
            return json(switch(polls++){case 0->"{\"error\":\"authorization_pending\"}";case 1->"{\"error\":\"slow_down\"}";default->"{\"access_token\":\"MS-SECRET\"}";});
        }
        public JsonObject post(String url,JsonObject body)throws Exception{
            if(url.contains("xsts")){if(failXbox)throw new IOException("Service returned HTTP 401 at xsts.auth.xboxlive.com");return json("{\"Token\":\"XSTS-SECRET\",\"DisplayClaims\":{\"xui\":[{\"uhs\":\"userhash\"}]}}");}
            if(url.contains("user.auth"))return json("{\"Token\":\"XBOX-SECRET\"}");
            return json("{\"access_token\":\"MC-SECRET\",\"expires_in\":86400}");
        }
        public JsonObject get(String url,String token){assertEquals("MC-SECRET",token);if(url.contains("entitlements"))return json(owns?"{\"items\":[{}]}":"{\"items\":[]}");return json("{\"name\":\""+name+"\",\"id\":\"01234567890123456789012345678901\"}");}
    }
    @Test void deviceFlowSupportsAnotherAccountAndReportsProgress()throws Exception{
        var transport=new FakeAuth();List<Long> waits=new ArrayList<>();List<String> status=new ArrayList<>();
        var session=new MicrosoftAuth(transport,waits::add).login(code->{assertEquals("USERCODE",code.code());assertFalse(code.toString().contains("PRIVATECODE"));},status::add);
        assertEquals("SecondAccount",session.name());assertEquals(List.of(1000L,1000L,6000L),waits);assertEquals(7,status.size());
        assertTrue(status.stream().noneMatch(x->x.contains("SECRET")||x.contains("PRIVATECODE")));assertTrue(session.expiresAt()>System.currentTimeMillis());
    }
    @Test void ownershipFailureAndCancellationNeverCreateSessions(){
        var transport=new FakeAuth();transport.owns=false;
        var failed=assertThrows(IOException.class,()->new MicrosoftAuth(transport,n->{}).login(c->{},s->{}));assertTrue(failed.getMessage().contains("ownership"));assertFalse(failed.getMessage().contains("SECRET"));
        assertThrows(InterruptedException.class,()->new MicrosoftAuth(new FakeAuth(),n->{throw new InterruptedException();}).login(c->{},s->{}));
    }
    @Test void failedXboxStepGivesUsefulErrorWithoutTokens(){
        var transport=new FakeAuth();transport.failXbox=true;var failed=assertThrows(IOException.class,()->new MicrosoftAuth(transport,n->{}).login(c->{},s->{}));
        assertTrue(failed.getMessage().contains("Xbox account permissions"));assertTrue(failed.getMessage().contains("HTTP 401"));assertFalse(failed.getMessage().contains("SECRET"));
    }
    Path jar(String name,String id,String version)throws Exception{Path p=dir.resolve(name);try(var z=new ZipOutputStream(Files.newOutputStream(p))){z.putNextEntry(new ZipEntry("fabric.mod.json"));z.write(("{\"id\":\""+id+"\",\"version\":\""+version+"\"}").getBytes());z.closeEntry();}return p;}
}
