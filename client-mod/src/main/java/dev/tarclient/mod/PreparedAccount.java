package dev.tarclient.mod;
import dev.tarclient.launcher.MicrosoftAuth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.*;
import java.util.*;
import java.io.IOException;

/** All network validation finishes before leaving the old world or changing its identity. */
public record PreparedAccount(Session session,UserApiService api,UserApiService.UserProperties properties,ProfileResult profile) {
    public static PreparedAccount prepare(MicrosoftAuth.Session auth,MinecraftClient client) throws Exception {
        MicrosoftAuth.validate(auth);
        String id=auth.uuid().replace("-","");
        UUID uuid=UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})","$1-$2-$3-$4-$5"));
        var service=new YggdrasilAuthenticationService(client.getNetworkProxy());
        var api=service.createUserApiService(auth.accessToken());
        var properties=api.fetchProperties();
        var profile=service.createMinecraftSessionService().fetchProfile(uuid,true);
        if(profile==null||!profile.profile().id().equals(uuid))throw new IOException("Minecraft could not verify this profile. Try signing in again.");
        var session=new Session(profile.profile().name(),uuid,auth.accessToken(),Optional.empty(),Optional.of(MicrosoftAuth.DEFAULT_CLIENT_ID));
        return new PreparedAccount(session,api,properties,profile);
    }
    @Override public String toString(){return session.getUsername();}
}
