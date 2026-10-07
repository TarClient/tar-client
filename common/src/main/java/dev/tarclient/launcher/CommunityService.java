package dev.tarclient.launcher;
import com.google.gson.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.io.IOException;
public final class CommunityService {
    public static final String OWNER_UUID="ccb2c06282bc4afb86a71058b176dbef";
    public static final String ENDPOINT="https://tar-client-community.prutprut2003.workers.dev";
    private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    public record Connection(String token,long expires,String uuid) {public String toString(){return "Tar community session";}}
    public static boolean configured(){return ENDPOINT.startsWith("https://");}
    public static boolean owner(String uuid){return uuid!=null&&OWNER_UUID.equals(uuid.replace("-","").toLowerCase(Locale.ROOT));}
    private static JsonObject post(String path,JsonObject body,String token)throws Exception {
        if(!configured())throw new IOException("Tar community is not deployed yet.");
        var request=HttpRequest.newBuilder(URI.create(ENDPOINT+path)).timeout(Duration.ofSeconds(15)).header("Content-Type","application/json").header("User-Agent","TarClient/1.0.0");
        if(token!=null)request.header("Authorization","Bearer "+token);
        var response=HTTP.send(request.POST(HttpRequest.BodyPublishers.ofString(body.toString())).build(),HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()/100!=2){String message=switch(response.statusCode()){case 401->"Community sign-in expired. Retry.";case 403->"Only Tarrecool may manage ranks.";case 429->"Too many requests. Wait a minute and retry.";default->"Community request failed (HTTP "+response.statusCode()+").";};throw new IOException(message);}
        if(response.body().length()>131072)throw new IOException("Unexpected community response.");
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
    public static Connection connect(MicrosoftAuth.Session session)throws Exception {
        if(session.demo())throw new IOException("Sign in with a Minecraft Java account first.");
        String uuid=session.uuid().replace("-","");var challenge=new JsonObject();challenge.addProperty("uuid",uuid);challenge.addProperty("name",session.name());
        var proof=post("/v1/challenge",challenge,null);String serverId=proof.get("serverId").getAsString();if(!serverId.matches("[a-f0-9]{40}"))throw new IOException("Invalid community challenge.");
        // The Minecraft token goes only to Mojang. The community server sees the public proof.
        var join=new JsonObject();join.addProperty("accessToken",session.accessToken());join.addProperty("selectedProfile",uuid);join.addProperty("serverId",serverId);
        var request=HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/join")).timeout(Duration.ofSeconds(15)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(join.toString())).build();
        var response=HTTP.send(request,HttpResponse.BodyHandlers.discarding());if(response.statusCode()!=204)throw new IOException("Minecraft could not verify your community session. Sign in again.");
        var confirm=new JsonObject();confirm.add("id",proof.get("id"));var connection=post("/v1/confirm",confirm,null);
        if(!uuid.equals(connection.get("uuid").getAsString()))throw new IOException("Community identity mismatch.");
        return new Connection(connection.get("token").getAsString(),connection.get("expires").getAsLong(),uuid);
    }
    public static JsonObject heartbeat(Connection connection,List<String> players)throws Exception {var body=new JsonObject();body.add("players",Net.JSON.toJsonTree(players));return post("/v1/heartbeat",body,connection.token());}
    public static void leave(Connection connection)throws Exception {post("/v1/leave",new JsonObject(),connection.token());}
    public static JsonObject rank(Connection connection,String name,String rank)throws Exception {var body=new JsonObject();body.addProperty("name",name);body.addProperty("rank",rank);return post("/v1/rank",body,connection.token());}
    public static JsonObject ranks(Connection connection)throws Exception {return post("/v1/ranks",new JsonObject(),connection.token());}
}
