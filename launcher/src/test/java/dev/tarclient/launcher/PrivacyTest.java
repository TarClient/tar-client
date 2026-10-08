package dev.tarclient.launcher;

import dev.tarclient.config.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PrivacyTest {
    @TempDir Path dir;
    @Test void missingOrDamagedPrivacyChoiceNeverEnablesAutomaticSharing()throws Exception {
        var privacy=new PrivacyPreferences(dir);assertFalse(privacy.hasChoice());assertFalse(privacy.sharingEnabled());
        privacy.save(true);assertTrue(new PrivacyPreferences(dir).sharingEnabled());
        Files.writeString(dir.resolve("badge-sharing.txt"),"invalid");assertFalse(privacy.hasChoice());assertFalse(privacy.sharingEnabled());
        privacy.save(false);assertTrue(privacy.hasChoice());assertFalse(privacy.sharingEnabled());
    }
    @Test void changingModuleProfilesCannotUndoTheInstallationOptOut()throws Exception {
        var privacy=new PrivacyPreferences(dir);privacy.save(false);
        var config=new ClientConfig();config.set("badges","enabled",true);
        var profiles=new ProfileStore(dir);profiles.create("All badges",config);profiles.load("All badges");profiles.presets();
        assertFalse(new PrivacyPreferences(dir).sharingEnabled());
    }
    @Test void serviceRejectsRequestsBeforeNetworkWhenSharingIsDisabled()throws Exception {
        String previous=System.getProperty("tar.data");System.setProperty("tar.data",dir.toString());
        try{
            new PrivacyPreferences(dir).save(false);
            var error=assertThrows(java.io.IOException.class,()->CommunityService.heartbeat(new CommunityService.Connection("not-a-real-token",0,"a".repeat(32)),List.of("a".repeat(32))));
            assertTrue(error.getMessage().contains("Enable badge sharing"));
        }finally{if(previous==null)System.clearProperty("tar.data");else System.setProperty("tar.data",previous);}
    }
}
