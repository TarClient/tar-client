package dev.tarclient.launcher;

import com.google.gson.*;
import java.util.*;
import java.util.function.*;
import java.io.*;

/** Browser-based Microsoft device authorization. Tokens live only in this process. */
public final class MicrosoftAuth {
    interface Transport {
        JsonObject form(String url,Map<String,String> values) throws Exception;
        JsonObject post(String url,JsonObject value) throws Exception;
        JsonObject get(String url,String token) throws Exception;
    }
    interface Sleeper { void sleep(long millis) throws InterruptedException; }
    private final Transport transport;
    private final Sleeper sleeper;
    public MicrosoftAuth(){this(new Transport(){
        public JsonObject form(String url,Map<String,String> values)throws Exception{return Net.form(url,values);}
        public JsonObject post(String url,JsonObject value)throws Exception{return Net.post(url,value);}
        public JsonObject get(String url,String token)throws Exception{return Net.send(Net.request(url).header("Authorization","Bearer "+token).GET().build()).getAsJsonObject();}
    },Thread::sleep);}
    MicrosoftAuth(Transport transport,Sleeper sleeper){this.transport=transport;this.sleeper=sleeper;}
    /** Public desktop application identifier; never a password or client secret. */
    public static final String DEFAULT_CLIENT_ID="c8d8f6e2-12dc-4499-911c-1c7294e91f44";
    static String clientId(String override) {
        return DEFAULT_CLIENT_ID; // Ignore obsolete per-installation overrides.
    }
    public record Session(String name,String uuid,String accessToken,long expiresAt,boolean demo) {
        public static Session demoSession() { return new Session("DemoPlayer","00000000000000000000000000000000","0",Long.MAX_VALUE,true); }
        @Override public String toString() { return name+(demo?" (demo)":""); }
    }
    public record DeviceCode(String code,String url,int expiresIn) {}
    public Session login(String clientId,Consumer<DeviceCode> showCode) throws Exception {return login(showCode,s->{});}
    public Session login(Consumer<DeviceCode> showCode,Consumer<String> progress) throws Exception {
        String stage="Requesting a Microsoft sign-in code";
        try {
        progress.accept(stage);
        String clientId=DEFAULT_CLIENT_ID;
        clientId=clientId(clientId);
        String root="https://login.microsoftonline.com/consumers/oauth2/v2.0/";
        var device=transport.form(root+"devicecode",Map.of("client_id",clientId.trim(),"scope","XboxLive.signin"));
        check(device);
        int seconds=device.get("expires_in").getAsInt(), interval=device.get("interval").getAsInt();
        showCode.accept(new DeviceCode(device.get("user_code").getAsString(),device.get("verification_uri").getAsString(),seconds));
        stage="Waiting for you to finish in the Microsoft browser page";progress.accept(stage);
        long deadline=System.currentTimeMillis()+seconds*1000L; String ms=null;
        while(System.currentTimeMillis()<deadline) {
            sleeper.sleep(interval*1000L);
            var token=transport.form(root+"token",Map.of("grant_type","urn:ietf:params:oauth:grant-type:device_code","client_id",clientId.trim(),"device_code",device.get("device_code").getAsString()));
            if(!token.has("error")) { ms=token.get("access_token").getAsString(); break; }
            String err=token.get("error").getAsString();
            if(err.equals("authorization_pending")) continue;
            if(err.equals("slow_down")) { interval+=5; continue; }
            check(token);
        }
        if(ms==null) throw new IOException("Sign-in expired. Please try again.");
        stage="Connecting your Xbox profile";progress.accept(stage);
        var xboxBody=new JsonObject(); var p=new JsonObject();
        p.addProperty("AuthMethod","RPS"); p.addProperty("SiteName","user.auth.xboxlive.com"); p.addProperty("RpsTicket","d="+ms);
        xboxBody.add("Properties",p); xboxBody.addProperty("RelyingParty","http://auth.xboxlive.com"); xboxBody.addProperty("TokenType","JWT");
        var xbox=transport.post("https://user.auth.xboxlive.com/user/authenticate",xboxBody);
        var xstsBody=new JsonObject(); p=new JsonObject(); p.addProperty("SandboxId","RETAIL");
        var tokens=new JsonArray(); tokens.add(xbox.get("Token").getAsString()); p.add("UserTokens",tokens);
        xstsBody.add("Properties",p); xstsBody.addProperty("RelyingParty","rp://api.minecraftservices.com/"); xstsBody.addProperty("TokenType","JWT");
        stage="Checking Xbox account permissions";progress.accept(stage);
        var xsts=transport.post("https://xsts.auth.xboxlive.com/xsts/authorize",xstsBody);
        String uhs=xsts.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        var mcBody=new JsonObject(); mcBody.addProperty("identityToken","XBL3.0 x="+uhs+";"+xsts.get("Token").getAsString());
        stage="Connecting to Minecraft services";progress.accept(stage);
        var mc=transport.post("https://api.minecraftservices.com/authentication/login_with_xbox",mcBody);
        String access=mc.get("access_token").getAsString();
        stage="Checking Minecraft Java ownership";progress.accept(stage);
        var entitlements=transport.get("https://api.minecraftservices.com/entitlements/mcstore",access);
        if(!entitlements.has("items")||entitlements.getAsJsonArray("items").isEmpty())throw new IOException("This Microsoft account has no active Minecraft Java entitlement. Use the demo option or sign in with an eligible account.");
        stage="Loading your Minecraft Java profile";progress.accept(stage);
        // A successful profile response is required: no offline account impersonation or ownership bypass.
        var profile=transport.get("https://api.minecraftservices.com/minecraft/profile",access);
        return new Session(profile.get("name").getAsString(),profile.get("id").getAsString(),access,System.currentTimeMillis()+mc.get("expires_in").getAsLong()*1000L,false);
        }catch(InterruptedException e){throw e;}
        catch(Exception e){throw new IOException(stage+" failed. "+(e instanceof IOException?e.getMessage():"Check your connection and try again.")+" If Microsoft selected the wrong account, retry using a private browser window. Each account needs its own Minecraft Java access.",e);}
    }
    public static String failureMessage(Throwable failure){
        if(failure instanceof IOException)return failure.getMessage();
        return "Could not complete sign-in. Check your connection and try again.";
    }
    public static void validate(Session session) throws Exception {
        if(session.demo()||session.accessToken().isBlank()||session.expiresAt()<=System.currentTimeMillis())throw new IOException("Please sign in again; this account session has expired.");
        var entitlement=Net.send(Net.request("https://api.minecraftservices.com/entitlements/mcstore").header("Authorization","Bearer "+session.accessToken()).GET().build()).getAsJsonObject();
        if(!entitlement.has("items")||entitlement.getAsJsonArray("items").isEmpty())throw new IOException("This account has no active Minecraft Java entitlement.");
        var profile=Net.send(Net.request("https://api.minecraftservices.com/minecraft/profile").header("Authorization","Bearer "+session.accessToken()).GET().build()).getAsJsonObject();
        if(!profile.get("id").getAsString().replace("-", "").equalsIgnoreCase(session.uuid().replace("-", "")))throw new IOException("Minecraft profile does not match this account. Please sign in again.");
    }
    private void check(JsonObject result) throws IOException {
        if(result.has("error")) throw new IOException("Microsoft sign-in: "+result.get("error").getAsString()+". Tar Client includes its application ID. Try again; if it continues, report this error to Tar support.");
    }
}
