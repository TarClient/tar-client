package dev.tarclient.launcher;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BadgeRanksTest {
    @Test void newBadgesSurviveLookupAndUnknownOrUnrequestedRanksDoNot() throws Exception {
        var ids=List.of("a".repeat(32),"b".repeat(32),"c".repeat(32),"d".repeat(32));
        var found=CommunityService.lookupPlayers(ids,batch->{
            var players=new JsonObject();
            players.addProperty(ids.get(0),"donator");players.addProperty(ids.get(1),"coowner");players.addProperty(ids.get(2),"owner");
            players.addProperty(ids.get(3),"unrecognized");players.addProperty("e".repeat(32),"owner");
            var response=new JsonObject();response.add("players",players);return response;
        });
        assertEquals(3,found.size());assertEquals("owner",found.get(ids.get(2)));
        assertEquals("donator",found.get(ids.get(0)));assertEquals("coowner",found.get(ids.get(1)));
    }
    @Test void coOwnerMenuOnlyOffersDelegatedRanksAndOwnerIsNeverAssignable() {
        assertEquals(List.of(BadgeRank.PARTNER,BadgeRank.MOD),BadgeRank.assignable(false));
        assertFalse(BadgeRank.assignable(true).contains(BadgeRank.OWNER));
        assertTrue(BadgeRank.assignable(true).contains(BadgeRank.CO_OWNER));
        assertNotEquals(BadgeRank.OWNER.color,BadgeRank.CO_OWNER.color);
        assertNotEquals(BadgeRank.DONATOR.color,BadgeRank.NORMAL.color);
    }
}
