package dev.tarclient.launcher;
import com.google.gson.JsonObject;
/** Call from a worker thread. Never writes Minecraft tokens or community sessions to disk. */
public final class SocialClient {
    private CommunityService.Connection connection;
    public synchronized JsonObject request(MicrosoftAuth.Session session,String action,JsonObject body)throws Exception {
        if(session==null||session.demo())throw new java.io.IOException("Sign in with a Minecraft Java account first.");
        String id=session.uuid().replace("-","");
        if(connection==null||!connection.uuid().equals(id)||connection.expires()<System.currentTimeMillis()+60000)connection=CommunityService.connectSocial(session);
        try{return CommunityService.social(connection,action,body);}
        catch(CommunityService.RequestException error){if(error.status!=401)throw error;connection=CommunityService.connectSocial(session);return CommunityService.social(connection,action,body);}
    }
    public static JsonObject body(String... pairs){var result=new JsonObject();for(int i=0;i<pairs.length;i+=2)result.addProperty(pairs[i],pairs[i+1]);return result;}
}
