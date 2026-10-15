package dev.tarclient.launcher;
import dev.tarclient.config.*;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class SocialTest {
    @Test void everyRequestedItemIsAvailableAndHasAUniqueId(){
        assertEquals(16,SocialCatalog.EMOTES.size());assertEquals(20,SocialCatalog.COSMETICS.size());
        var ids=new HashSet<String>();for(var item:SocialCatalog.EMOTES){assertTrue(ids.add(item.id()));assertTrue(item.toString().endsWith("Free"));}for(var item:SocialCatalog.COSMETICS){assertTrue(ids.add(item.id()));assertTrue(item.toString().endsWith("Free"));}
    }
    @Test void equippingReplacesOnlyItsSlotAndClickingAgainRemoves(){
        var initial=List.of("angelhalo","angelwings","katana","basiccape","blackbandana");
        var updated=SocialCatalog.equip(initial,"bluehalo");assertFalse(updated.contains("angelhalo"));assertTrue(updated.containsAll(List.of("bluehalo","angelwings","katana","basiccape","blackbandana")));
        assertEquals(4,SocialCatalog.equip(updated,"bluehalo").size());assertThrows(NoSuchElementException.class,()->SocialCatalog.equip(initial,"paid-hack"));
    }
    @Test void animationsRemainFiniteAndDoNotRequireGameMovement()throws Exception{
        for(var item:SocialCatalog.EMOTES)for(double time:new double[]{0,.1,1,1.4,2.2,5,14.9,119.9}){var p=EmotePose.at(item.id(),time);for(var field:EmotePose.class.getFields())assertTrue(Float.isFinite(field.getFloat(p)),item.id());}
        assertTrue(EmotePose.at("ironman",1).lift>0);assertTrue(EmotePose.at("sit",1).lift<0);assertEquals(0,EmotePose.at("unknown",1).lift);
    }
    @Test void friendStatePreservesPlainTextAndBlockedAccounts(){
        var state=SocialState.parse(JsonParser.parseString("{\"relations\":[{\"uuid\":\"a\",\"name\":\"Alice\",\"kind\":\"incoming\",\"online\":false}],\"blocked\":[{\"uuid\":\"b\",\"name\":\"Bob\"}],\"look\":[\"angelwings\"]}").getAsJsonObject());
        assertEquals("incoming",state.people().getFirst().kind());assertEquals("blocked",state.people().getLast().kind());assertEquals(List.of("angelwings"),state.cosmetics());
        var m=SocialState.messages(JsonParser.parseString("{\"messages\":[{\"sender\":\"a\",\"text\":\"<html>hello</html>\",\"created\":123}]}").getAsJsonObject());assertEquals("<html>hello</html>",m.getFirst().text());
    }
    @Test void backendAndClientCatalogsStayCompatible()throws Exception{
        Path source=Files.isDirectory(Path.of("community-service"))?Path.of("community-service/social.mjs"):Path.of("../community-service/social.mjs");
        if(!Files.exists(source))source=Path.of("outputs/TarClient-source/community-service/social.mjs");
        String backend=Files.readString(source);for(var item:SocialCatalog.COSMETICS)assertTrue(backend.contains(item.id()+":'"+item.slot()+"'"));for(var item:SocialCatalog.EMOTES)assertTrue(backend.contains("'"+item.id()+"'"));
    }
    @Test void demoCannotUseSocialServiceAndUnknownActionsNeverReachNetwork(){
        assertThrows(java.io.IOException.class,()->new SocialClient().request(MicrosoftAuth.Session.demoSession(),"state",new JsonObject()));
        assertThrows(java.io.IOException.class,()->CommunityService.social(new CommunityService.Connection("",0,""),"rank",new JsonObject()));
    }
}
