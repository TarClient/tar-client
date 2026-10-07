package dev.tarclient.launcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import dev.tarclient.config.ClientConfig;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class CoreTest {
    @TempDir Path temp;
    @Test void streamerCoordinatesReplaceAllPositionLinesButPreserveOtherDebugInfo() {
        var p=new dev.tarclient.config.StreamerCoordinates.Position(-17,81,34);
        for(String line:List.of("XYZ: 123456.789 / 77.12345 / 98765.432","Block: 123456 77 98765","Chunk: 7716 4 6172 [4 28 in r.241.192.mca]","Section-relative: 00 13 09","Targeted Block: 123456, 77, 98765","Targeted Fluid: 123456, 77, 98765")){
            String masked=dev.tarclient.config.StreamerCoordinates.vanilla(line,p);
            assertTrue(masked.endsWith("[Streamer]"));assertFalse(masked.contains("123456"));assertFalse(masked.contains("r.241.192"));
        }
        assertEquals("Chunk: -2 5 2 [Streamer]",dev.tarclient.config.StreamerCoordinates.vanilla("Chunk: 123456",p));
        assertEquals("Section-relative: 15 1 2 [Streamer]",dev.tarclient.config.StreamerCoordinates.vanilla("Section-relative: 123456",p));
        for(String other:List.of("144 fps T: 120","Facing: north","minecraft:overworld","Biome: minecraft:plains","Java: 21.0.10"))assertEquals(other,dev.tarclient.config.StreamerCoordinates.vanilla(other,p));
        for(String id:List.of("player_coords","block_coords","chunk_relative_coords","chunk_coords","targeted_block","targeted_fluid","targeted_entity"))assertNotNull(dev.tarclient.config.StreamerCoordinates.betterF3(id,p));
        assertNull(dev.tarclient.config.StreamerCoordinates.betterF3("fps",p));
    }
    @Test void streamerPreferenceSurvivesCustomProfiles() throws Exception {
        var c=new ClientConfig();assertFalse(c.on("streamer"));c.set("streamer","enabled",true);
        var store=new dev.tarclient.config.ProfileStore(temp);store.create("Streaming",c);assertTrue(store.load("Streaming").on("streamer"));
    }
    @Test void expiredAndDemoAccountsCannotBeUsedForSwitching() {
        assertThrows(java.io.IOException.class,()->MicrosoftAuth.validate(MicrosoftAuth.Session.demoSession()));
        assertThrows(java.io.IOException.class,()->MicrosoftAuth.validate(new MicrosoftAuth.Session("Test","id","expired",0,false)));
        assertFalse(new MicrosoftAuth.Session("Test","id","private-token",Long.MAX_VALUE,false).toString().contains("private-token"));
    }
    @Test void desktopInstallationCopiesWholeAppWithoutOverwritingOldVersion() throws Exception {
        Path source=temp.resolve("extracted");
        for(String name:List.of("Tar Client.exe","app/tar-launcher.jar","app/Tar Client.cfg","runtime/bin/java.exe","runtime/lib/modules","licenses/LICENSE.txt")){
            Path p=source.resolve(name);Files.createDirectories(p.getParent());Files.writeString(p,"content of "+name);
        }
        Path installed=DesktopInstaller.copyApplication(source,temp.resolve("Programs/Tar Client"));
        assertEquals("content of runtime/lib/modules",Files.readString(installed.getParent().resolve("runtime/lib/modules")));
        assertEquals("content of app/tar-launcher.jar",Files.readString(installed.getParent().resolve("app/tar-launcher.jar")));
        Path second=DesktopInstaller.copyApplication(source,temp.resolve("Programs/Tar Client"));
        assertNotEquals(installed,second);assertTrue(Files.exists(installed));assertTrue(Files.exists(source.resolve("Tar Client.exe")));
        assertThrows(java.io.IOException.class,()->DesktopInstaller.copyApplication(source,source.resolve("nested")));
        assertEquals("'C:\\Users\\O''Brien\\Tar Client.exe'",DesktopInstaller.quote("C:\\Users\\O'Brien\\Tar Client.exe"));
    }
    @Test void incompleteDownloadsCannotCreateAnInstalledApp() throws Exception {
        Path source=temp.resolve("partial");Files.createDirectories(source);Files.writeString(source.resolve("Tar Client.exe"),"exe");
        assertThrows(java.io.IOException.class,()->DesktopInstaller.copyApplication(source,temp.resolve("Programs")));
        assertFalse(Files.exists(temp.resolve("Programs")));
    }
    @Test void version040BacksUpVersion031() throws Exception {
        Path mods=temp.resolve("mods");Files.createDirectories(mods);Path old=mods.resolve("tar-client-0.3.1.jar");
        Files.copy(jar("old040.jar","{\"id\":\"tarclient\",\"version\":\"0.3.1\"}","fabric.mod.json"),old);
        byte[] update=Files.readAllBytes(jar("update040.jar","{\"id\":\"tarclient\",\"version\":\"0.4.0\"}","fabric.mod.json"));
        CoreInstaller.install(temp,new java.io.ByteArrayInputStream(update),"0.4.0");assertFalse(Files.exists(old));assertArrayEquals(update,Files.readAllBytes(mods.resolve("tar-client-0.4.0.jar")));
    }
    @Test void customProfilesCreateWithoutOverwritingAndKeepNewSettings() throws Exception {
        var store=new dev.tarclient.config.ProfileStore(temp);var c=new ClientConfig();
        c.set("armor","horizontal",false);c.set("armor","reverse",true);c.set("armor","iconSize",24);c.set("armor","text",false);
        c.set("motionblur","enabled",true);c.set("motionblur","strength",63);c.set("motionblur","pauseInGuis",false);
        store.create("My Survival",c);c.set("motionblur","strength",18);
        assertThrows(java.io.IOException.class,()->store.create("my survival",c));
        assertThrows(java.io.IOException.class,()->store.create("My Survival",c));
        var loaded=store.load("My Survival");assertEquals(63,loaded.number("motionblur","strength"));assertFalse(loaded.bool("motionblur","pauseInGuis"));
        assertFalse(loaded.bool("armor","horizontal"));assertTrue(loaded.bool("armor","reverse"));assertEquals(24,loaded.i("armor","iconSize"));assertFalse(loaded.bool("armor","text"));
        store.save("My Survival",c);assertEquals(18,store.load("My Survival").i("motionblur","strength"));
        assertEquals(List.of("My Survival"),store.list());
    }
    @Test void upgradeRemovesSpotifyButPreservesOtherSettings() throws Exception {
        Path p=temp.resolve("old.json");Files.writeString(p,"{\"spotify\":{\"enabled\":true},\"armor\":{\"horizontal\":false},\"motionblur\":{\"enabled\":true}}");
        var c=ClientConfig.read(p);assertFalse(c.bool("armor","horizontal"));assertTrue(c.on("motionblur"));assertEquals(20,c.i("motionblur","strength"));
        c.set("motionblur","strength",999);assertEquals(100,c.i("motionblur","strength"));
        c.save(p);assertFalse(JsonParser.parseString(Files.readString(p)).getAsJsonObject().has("spotify"));
        assertTrue(ClientConfig.MODULES.stream().noneMatch(m->m.id().equals("spotify")));
    }
    @Test void armorDirectionsKeepEverySlotInsideHudBounds() {
        for(boolean horizontal:List.of(false,true))for(boolean reverse:List.of(false,true))for(int count=1;count<=4;count++){
            var layout=new dev.tarclient.config.ArmorLayout(count,horizontal,reverse,37,29,8);
            for(int i=0;i<count;i++){
                assertTrue(layout.x(i)>=4&&layout.x(i)+37<=layout.width()-4);
                assertTrue(layout.y(i)>=4&&layout.y(i)+29<=layout.height()-4);
                if(i>0){int difference=horizontal?layout.x(i)-layout.x(i-1):layout.y(i)-layout.y(i-1);assertEquals((reverse?-1:1)*((horizontal?37:29)+8),difference);}
            }
        }
    }
    @Test void version031BacksUpVersion030() throws Exception {
        Path mods=temp.resolve("mods");Files.createDirectories(mods);Path old=mods.resolve("tar-client-0.3.0.jar");
        Files.copy(jar("old.jar","{\"id\":\"tarclient\",\"version\":\"0.3.0\"}","fabric.mod.json"),old);
        byte[] update=Files.readAllBytes(jar("update.jar","{\"id\":\"tarclient\",\"version\":\"0.3.1\"}","fabric.mod.json"));
        CoreInstaller.install(temp,new java.io.ByteArrayInputStream(update),"0.3.1");assertFalse(Files.exists(old));assertArrayEquals(update,Files.readAllBytes(mods.resolve("tar-client-0.3.1.jar")));
        try(var backups=Files.list(temp.resolve("removed-mods"))){assertEquals(1,backups.count());}
    }
    @Test void profilesKeepModuleSettingsAndRejectEscapingNames() throws Exception {
        var store=new dev.tarclient.config.ProfileStore(temp);store.presets();
        var bedwars=store.load("Bedwars");assertTrue(bedwars.on("keys"));assertFalse(bedwars.on("fullbright"));
        var smp=store.load("SMP");assertTrue(smp.on("fullbright"));assertFalse(smp.on("keys"));
        bedwars.set("armor","showBackground",true);bedwars.set("zoom","factor",7);store.save("Custom",bedwars);
        var restored=store.load("Custom");assertTrue(restored.bool("armor","showBackground"));assertEquals(7,restored.number("zoom","factor"));
        restored.set("keys","enabled",false);assertTrue(store.load("Custom").on("keys"));
        store.save("Bedwars",restored);store.presets();assertFalse(store.load("Bedwars").on("keys"));
        for(String bad:List.of("../escape","C:/escape","","a/b","..\\escape"))assertThrows(java.io.IOException.class,()->store.save(bad,restored));
    }
    @Test void oldSettingsAcquireEveryHudBackgroundSwitch() throws Exception {
        Path file=temp.resolve("old.json");Files.writeString(file,"{\"fps\":{\"enabled\":true,\"background\":\"80000000\"}}");
        var c=ClientConfig.read(file);assertTrue(c.on("fps"));assertEquals("80000000",c.text("fps","background"));
        for(var module:ClientConfig.MODULES)if(module.category().equals("HUD")){assertEquals(module.id().equals("potions"),c.bool(module.id(),"showBackground"));c.set(module.id(),"showBackground",true);assertTrue(c.bool(module.id(),"showBackground"));}
        assertTrue(c.bool("armor","horizontal"));assertEquals(4,c.number("zoom","factor"));
    }
    @Test void latestUpgradeBacksUpPreviousPatch() throws Exception {
        Path mods=temp.resolve("mods");Files.createDirectories(mods);
        byte[] old=Files.readAllBytes(jar("previous-patch.jar","{\"id\":\"tarclient\",\"version\":\"0.2.1\"}","fabric.mod.json"));Files.write(mods.resolve("tar-client-0.2.1.jar"),old);
        byte[] update=Files.readAllBytes(jar("new-core.jar","{\"id\":\"tarclient\",\"version\":\"0.3.0\"}","fabric.mod.json"));
        CoreInstaller.install(temp,new java.io.ByteArrayInputStream(update),"0.3.0");assertFalse(Files.exists(mods.resolve("tar-client-0.2.1.jar")));assertArrayEquals(update,Files.readAllBytes(mods.resolve("tar-client-0.3.0.jar")));
        try(var backups=Files.list(temp.resolve("removed-mods"))){assertArrayEquals(old,Files.readAllBytes(backups.findFirst().orElseThrow()));}
    }
    @Test void rejectsTraversalAndAbsoluteFilenames() {
        for(String bad:List.of("../escape.jar","C:/escape.jar","a/../../escape","..\\escape.jar","stream:ads"))assertThrows(Exception.class,()->Net.child(temp,bad));
        assertDoesNotThrow(()->Net.child(temp,"valid.jar"));
    }
    @Test void honorsOrderedRulesAndFeatures() {
        JsonObject rules=JsonParser.parseString("{\"rules\":[{\"action\":\"allow\"},{\"action\":\"disallow\",\"os\":{\"name\":\"windows\"}}]}").getAsJsonObject();
        assertFalse(GameInstaller.allowed(rules));
        JsonObject demo=JsonParser.parseString("{\"rules\":[{\"action\":\"allow\",\"features\":{\"is_demo_user\":true}}]}").getAsJsonObject();
        assertFalse(GameInstaller.allowed(demo));assertTrue(GameInstaller.allowed(demo,Map.of("is_demo_user",true)));
    }
    @Test void settingsRoundTripClampAndPerItem() throws Exception {
        var c=new ClientConfig();c.set("armor","threshold",999);assertEquals(50,c.number("armor","threshold"));
        c.set("items","overrides","minecraft:diamond_sword=0.35;minecraft:apple=NaN");assertEquals(0.35,c.itemScale("minecraft:diamond_sword","hand"));assertEquals(0.8,c.itemScale("minecraft:apple","hand"));
        Path file=temp.resolve("settings.json");c.save(file);assertEquals(50,ClientConfig.read(file).number("armor","threshold"));
        Files.writeString(file,"not json");assertThrows(Exception.class,()->ClientConfig.read(file));
    }
    @Test void fabricVersionRangesAndAlternatives() throws Exception {
        assertTrue(ModManager.matches(new JsonPrimitive(">=1.21.10 <1.22"),"1.21.11"));assertFalse(ModManager.matches(new JsonPrimitive("1.21.1"),"1.21.11"));assertTrue(ModManager.matches(new JsonPrimitive("~1.21.1"),"1.21.11"));
        assertTrue(ModManager.matches(JsonParser.parseString("[\"1.21.10\",\"1.21.11\"]"),"1.21.11"));
    }
    @Test void localImportsRejectForgeWrongVersionAndDuplicates() throws Exception {
        var m=new ModManager(temp.resolve("game"),s->{});
        Path forge=jar("forge.jar","{}","META-INF/mods.toml");assertThrows(Exception.class,()->m.importJar(forge));
        Path wrong=jar("wrong.jar","{\"id\":\"test\",\"version\":\"1.0\",\"depends\":{\"minecraft\":\"1.20.1\"}}","fabric.mod.json");assertThrows(Exception.class,()->m.importJar(wrong));
        Path valid=jar("valid.jar","{\"id\":\"test\",\"version\":\"1.0\",\"depends\":{\"minecraft\":\"1.21.11\"}}","fabric.mod.json");m.importJar(valid);m.preflight();assertThrows(Exception.class,()->m.importJar(valid));
        m.toggle(m.list().getFirst());assertTrue(m.list().getFirst().toString().endsWith(".disabled"));m.toggle(m.list().getFirst());m.preflight();
    }
    @Test void missingDependenciesBlockLaunch() throws Exception {
        var m=new ModManager(temp.resolve("game"),s->{});m.importJar(jar("dependent.jar","{\"id\":\"test\",\"version\":\"1.0\",\"depends\":{\"missing\":\"*\"}}","fabric.mod.json"));assertThrows(Exception.class,m::preflight);
    }
    @Test void demoSessionIsExplicitAndTokenNotPrinted() {var demo=MicrosoftAuth.Session.demoSession();assertTrue(demo.demo());var live=new MicrosoftAuth.Session("Player","id","TOP-SECRET",0,false);assertFalse(live.toString().contains("TOP-SECRET"));}
    @Test void microsoftIdWorksForFreshAndLegacyPreferences() {
        assertEquals("c8d8f6e2-12dc-4499-911c-1c7294e91f44",MicrosoftAuth.clientId(null));
        for(String empty:List.of("", "  ", "\t\n"))assertEquals(MicrosoftAuth.DEFAULT_CLIENT_ID,MicrosoftAuth.clientId(empty));
        assertEquals(MicrosoftAuth.DEFAULT_CLIENT_ID,MicrosoftAuth.clientId(" custom-application-id "));
    }
    @Test void patchUpgradeRemovesBothPreviousCoresWithoutTouchingOtherMods() throws Exception {
        Path mods=temp.resolve("mods");Files.createDirectories(mods);
        for(String previous:List.of("0.1.0","0.2.0")) {
            Files.copy(jar("old-"+previous+".jar","{\"id\":\"tarclient\",\"version\":\""+previous+"\"}","fabric.mod.json"),mods.resolve("tar-client-"+previous+".jar"));
        }
        Path other=mods.resolve("other.jar");Files.writeString(other,"keep this mod");
        byte[] update=Files.readAllBytes(jar("patch.jar","{\"id\":\"tarclient\",\"version\":\"0.2.1\"}","fabric.mod.json"));
        for(int i=0;i<2;i++)CoreInstaller.install(temp,new java.io.ByteArrayInputStream(update),"0.2.1");
        assertArrayEquals(update,Files.readAllBytes(mods.resolve("tar-client-0.2.1.jar")));
        assertFalse(Files.exists(mods.resolve("tar-client-0.1.0.jar")));
        assertFalse(Files.exists(mods.resolve("tar-client-0.2.0.jar")));
        assertEquals("keep this mod",Files.readString(other));
        try(var backups=Files.list(temp.resolve("removed-mods"))){assertEquals(2,backups.count());}
    }
    @Test void coreUpgradePreservesWorldsAndBacksUpPreviousCore() throws Exception {
        Path game=temp.resolve("game"),mods=game.resolve("mods");Files.createDirectories(mods);
        Path old=jar("previous.jar","{\"id\":\"tarclient\",\"version\":\"0.1.0\"}","fabric.mod.json");
        Files.copy(old,mods.resolve("tar-client-0.1.0.jar"));
        Path world=game.resolve("saves/My world/level.dat");Files.createDirectories(world.getParent());Files.writeString(world,"world data");
        Path other=mods.resolve("my-mod.jar");Files.writeString(other,"unrelated mod");
        byte[] update=Files.readAllBytes(jar("update.jar","{\"id\":\"tarclient\",\"version\":\"0.2.0\"}","fabric.mod.json"));
        for(int i=0;i<2;i++)CoreInstaller.install(game,new java.io.ByteArrayInputStream(update),"0.2.0");
        assertArrayEquals(update,Files.readAllBytes(mods.resolve("tar-client-0.2.0.jar")));
        assertFalse(Files.exists(mods.resolve("tar-client-0.1.0.jar")));
        assertEquals("world data",Files.readString(world));assertEquals("unrelated mod",Files.readString(other));
        try(var backups=Files.list(game.resolve("removed-mods"))){var saved=backups.toList();assertEquals(1,saved.size());assertArrayEquals(Files.readAllBytes(old),Files.readAllBytes(saved.getFirst()));}
    }
    @Test void missingBundledCoreLeavesPreviousInstallUntouched() throws Exception {
        Path old=temp.resolve("mods/tar-client-0.1.0.jar");Files.createDirectories(old.getParent());Files.writeString(old,"original");
        assertThrows(java.io.IOException.class,()->CoreInstaller.install(temp,null,"0.2.0"));assertEquals("original",Files.readString(old));
    }
    @Test void decodesWebpModrinthIcons() throws Exception {
        // Original 2x2 solid-color fixture, encoded losslessly as WebP.
        byte[] bytes=Base64.getDecoder().decode("UklGRh4AAABXRUJQVlA4TBEAAAAvAUAAAAdQvFJUpv+BiOh/AAA=");
        try(var input=new javax.imageio.stream.MemoryCacheImageInputStream(new java.io.ByteArrayInputStream(bytes))){
            var readers=javax.imageio.ImageIO.getImageReaders(input);assertTrue(readers.hasNext());var reader=readers.next();
            try{reader.setInput(input);var image=reader.read(0);assertEquals(2,image.getWidth());assertEquals(2,image.getHeight());assertEquals(0xFF147832,image.getRGB(0,0));}finally{reader.dispose();}
        }
    }
    private Path jar(String name,String content,String entry)throws Exception{Path p=temp.resolve(name);try(var zip=new ZipOutputStream(Files.newOutputStream(p))){zip.putNextEntry(new ZipEntry(entry));zip.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));zip.closeEntry();}return p;}
}
