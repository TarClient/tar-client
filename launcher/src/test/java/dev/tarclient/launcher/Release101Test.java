package dev.tarclient.launcher;

import com.google.gson.JsonObject;
import dev.tarclient.config.ClientConfig;
import dev.tarclient.config.ProfileStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class Release101Test {
    @TempDir Path dir;

    @Test void crowdedServersResolveEveryPlayerWithoutTrustingUnrequestedBadges() throws Exception {
        var ids=new ArrayList<String>();
        for(int i=1;i<=251;i++)ids.add(String.format("%032x",i));
        ids.add(ids.getFirst());
        var seen=new ArrayList<String>();var batchSizes=new ArrayList<Integer>();
        var badges=CommunityService.lookupPlayers(ids,batch->{
            seen.addAll(batch);batchSizes.add(batch.size());
            var players=new JsonObject();for(String id:batch)players.addProperty(id,"normal");
            players.addProperty("f".repeat(32),"admin"); // Unrequested service data must not become a badge.
            var response=new JsonObject();response.add("players",players);return response;
        });
        assertEquals(List.of(100,100,51),batchSizes);
        assertEquals(251,new HashSet<>(seen).size());assertEquals(251,badges.size());
        assertEquals("normal",badges.get(String.format("%032x",251)));
        assertFalse(badges.containsKey("f".repeat(32)));
    }

    @Test void failedLaterBatchDoesNotReturnAPartialFreshPresenceSnapshot() {
        var ids=new ArrayList<String>();for(int i=1;i<=101;i++)ids.add(String.format("%032x",i));
        assertThrows(java.io.IOException.class,()->CommunityService.lookupPlayers(ids,batch->{
            if(batch.size()==1)throw new java.io.IOException("Service unavailable");
            var response=new JsonObject();response.add("players",new JsonObject());return response;
        }));
    }

    @Test void totemSizeSurvivesProfilesIndependentlyOfHeldItemSize() throws Exception {
        var config=new ClientConfig();assertTrue(config.on("badges"));
        config.set("totempop","enabled",true);config.set("totempop","size",35);
        config.set("items","enabled",true);config.set("items","hand",1.5);
        var store=new ProfileStore(dir);store.create("PvP",config);var restored=store.load("PvP");
        assertTrue(restored.on("totempop"));assertEquals(35,restored.i("totempop","size"));
        assertEquals(1.5,restored.number("items","hand"));
    }
}
