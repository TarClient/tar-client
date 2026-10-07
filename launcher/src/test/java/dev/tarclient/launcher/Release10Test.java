package dev.tarclient.launcher;
import dev.tarclient.config.*;
import com.google.gson.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class Release10Test {
    @TempDir Path dir;
    MicrosoftAuth.Session account(String id,String name){return new MicrosoftAuth.Session(name,id,"minecraft-private",1,false,"refresh-private");}
    @Test void rememberedAccountsSurviveReopenForgetAndSelectionChanges()throws Exception {
        AccountStore.Protector cipher=(data,encrypt)->{byte[] result=data.clone();for(int i=0;i<result.length;i++)result[i]^=0x55;return result;};
        var store=new AccountStore(dir,cipher);var a=account("a".repeat(32),"First");var b=account("b".repeat(32),"Second");
        store.remember(a,true);store.remember(b,false);
        var reopened=new AccountStore(dir,cipher).read();assertEquals(a.uuid(),reopened.selected());assertEquals(2,reopened.accounts().size());
        assertEquals("refresh-private",reopened.accounts().getFirst().refreshToken());
        assertFalse(new String(Files.readAllBytes(dir.resolve("accounts.dpapi"))).contains("refresh-private"));
        store.deselect();assertEquals("",store.read().selected());assertEquals(2,store.read().accounts().size());
        store.remember(b,true);store.forget(b.uuid());assertEquals("",store.read().selected());assertEquals(List.of(a),store.read().accounts());
        assertFalse(a.toString().contains("private"));
    }
    @Test @EnabledOnOs(OS.WINDOWS) void windowsEncryptionRoundTripUsesProtectedFile()throws Exception {
        var store=new AccountStore(dir);var saved=account("a".repeat(32),"EncryptedAccount");store.remember(saved,true);
        assertEquals(saved,new AccountStore(dir).read().accounts().getFirst());
        assertFalse(new String(Files.readAllBytes(dir.resolve("accounts.dpapi"))).contains("private"));
        store.forget(saved.uuid());assertTrue(store.read().accounts().isEmpty());
    }
    @Test void refreshRotatesTokenAndPreservesAccountIdentity()throws Exception {
        var transport=new Update041Test.FakeAuth(){@Override public JsonObject form(String url,Map<String,String> values){assertEquals("refresh_token",values.get("grant_type"));assertEquals("refresh-private",values.get("refresh_token"));return json("{\"access_token\":\"new-ms\",\"refresh_token\":\"rotated-private\"}");}};
        var session=new MicrosoftAuth(transport,n->{}).refresh(account("01234567890123456789012345678901","SecondAccount"),s->{});
        assertEquals("rotated-private",session.refreshToken());assertEquals("SecondAccount",session.name());
        assertThrows(java.io.IOException.class,()->new MicrosoftAuth(transport,n->{}).refresh(account("a".repeat(32),"Different"),s->{}));
    }
    @Test void invalidRefreshRequiresSignInWithoutReturningSecrets(){
        var transport=new Update041Test.FakeAuth(){@Override public JsonObject form(String url,Map<String,String> values){return json("{\"error\":\"invalid_grant\",\"error_description\":\"refresh-private\"}");}};
        var error=assertThrows(java.io.IOException.class,()->new MicrosoftAuth(transport,n->{}).refresh(account("a".repeat(32),"First"),s->{}));assertTrue(error.getMessage().contains("invalid_grant"));assertFalse(error.getMessage().contains("refresh-private"));
    }
    @Test void deletedPresetsStayDeletedAndHaveRecoverableContents()throws Exception {
        var profiles=new ProfileStore(dir);profiles.presets();var config=profiles.load("SMP");config.set("text","text","Custom saved setting");profiles.save("SMP",config);profiles.delete("SMP");profiles.presets();assertFalse(profiles.list().contains("SMP"));
        try(var files=Files.list(dir.resolve("tar-profiles/deleted"))){assertEquals("Custom saved setting",ClientConfig.read(files.findFirst().orElseThrow()).text("text","text"));}
        assertThrows(java.io.IOException.class,()->profiles.delete("../settings"));
    }
    @Test void itemExclusionsTakePrecedenceOverPerItemScaleAndContexts(){
        var c=new ClientConfig();c.set("items","overrides","minecraft:shield=2.0");c.set("items","filterMode","except listed");c.set("items","filterItems","shield, minecraft:diamond_sword");
        assertEquals(1,c.itemScale("minecraft:shield","hand"));assertEquals(0.8,c.itemScale("minecraft:apple","hand"));
        c.set("items","filterMode","only listed");assertEquals(2,c.itemScale("minecraft:shield","hand"));assertEquals(1,c.itemScale("minecraft:apple","hand"));
    }
    @Test void legacyGreenCrosshairMigratesOnceButCustomColorsSurvive()throws Exception {
        Path settings=dir.resolve("settings.json");Files.writeString(settings,"{\"crosshair\":{\"color\":\"A5F078\",\"outline\":true}}");var c=ClientConfig.read(settings);assertEquals("FFFFFF",c.text("crosshair","color"));assertFalse(c.bool("crosshair","outline"));
        c.set("crosshair","color","A5F078");c.save(settings);assertEquals("A5F078",ClientConfig.read(settings).text("crosshair","color"));
    }
    @Test void everyPanelModuleHasSelectableBackgroundsAndProfilesRetainNewSettings()throws Exception {
        var c=new ClientConfig();for(var m:ClientConfig.MODULES)if(m.settings().stream().anyMatch(s->s.key().equals("showBackground")))assertTrue(m.settings().stream().anyMatch(s->s.key().equals("backgroundStyle")),m.id());
        c.set("itemcounter","backgroundStyle","gradient");c.set("text","text","Line one\\nLine two");c.set("particles","crit",0);var profiles=new ProfileStore(dir);profiles.create("New",c);var restored=profiles.load("New");assertEquals("gradient",restored.text("itemcounter","backgroundStyle"));assertEquals(0,restored.i("particles","crit"));assertEquals("Line one\\nLine two",restored.text("text","text"));
    }
}
