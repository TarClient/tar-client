package dev.tarclient.mod;
import com.google.gson.*;
import dev.tarclient.config.*;
import dev.tarclient.launcher.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
public final class SocialRuntime {
    private static final ExecutorService WORK=Executors.newSingleThreadExecutor(r->Thread.ofVirtual().name("Tar friends").unstarted(r));
    private static final SocialClient API=new SocialClient();
    private static Map<String,Look> looks=Map.of();
    private static long next,validUntil,offset,epoch;
    private static boolean pending,published;
    private static String identity="";
    private static Object world;
    public record Look(List<String> cosmetics,String emote,long started,long until){}
    public static MicrosoftAuth.Session session(){var mc=MinecraftClient.getInstance();var s=mc.getSession();return new MicrosoftAuth.Session(s.getUsername(),s.getUuidOrNull()==null?"":s.getUuidOrNull().toString().replace("-",""),s.getAccessToken(),Long.MAX_VALUE,mc.isDemo());}
    public static void request(String action,JsonObject body,Consumer<JsonObject> success,Consumer<String> failure){
        var mc=MinecraftClient.getInstance();var auth=session();
        WORK.submit(()->{try{var result=API.request(auth,action,body);mc.execute(()->{if(session().uuid().equals(auth.uuid())){next=0;success.accept(result);}else failure.accept("Account changed; reopen Friends.");});}catch(Exception e){mc.execute(()->failure.accept(e.getMessage()==null?"Community service unavailable":e.getMessage()));}});
    }
    public static boolean sharing(){return PrivacyPreferences.defaults().sharingEnabled();}
    public static void tick(){
        var mc=MinecraftClient.getInstance();var auth=session();long now=System.currentTimeMillis();
        if(!identity.equals(auth.uuid())||world!=mc.world){identity=auth.uuid();world=mc.world;epoch++;looks=Map.of();next=0;}
        boolean active=mc.world!=null&&!auth.demo()&&auth.uuid().matches("[a-f0-9]{32}")&&sharing();
        if(!active){looks=Map.of();if(published&&!pending){published=false;pending=true;WORK.submit(()->{try{API.request(auth,"leave",new JsonObject());}catch(Exception ignored){}finally{mc.execute(()->pending=false);}});}return;}
        if(now>validUntil)looks=Map.of();
        if(pending||now<next)return;
        pending=true;next=now+5000;long generation=epoch;
        var ids=new LinkedHashSet<String>();ids.add(auth.uuid());
        // Rendered players only: a bounded request even on very large servers.
        mc.world.getPlayers().stream().sorted(Comparator.comparingDouble(p->mc.player==null?0:p.squaredDistanceTo(mc.player))).limit(99).forEach(p->ids.add(p.getUuidAsString().replace("-","")));
        var body=new JsonObject();var list=new JsonArray();ids.forEach(list::add);body.add("players",list);
        WORK.submit(()->{try{var response=API.request(auth,"presence",body);var found=new HashMap<String,Look>();
            for(var e:response.getAsJsonObject("players").entrySet()){var l=e.getValue().getAsJsonObject();found.put(e.getKey(),new Look(SocialState.strings(l.getAsJsonArray("cosmetics")),l.get("emote").getAsString(),l.get("started").getAsLong(),l.get("until").getAsLong()));}
            long clockOffset=response.get("now").getAsLong()-System.currentTimeMillis();
            mc.execute(()->{published=true;if(epoch==generation&&sharing()){looks=Map.copyOf(found);validUntil=System.currentTimeMillis()+15000;offset=clockOffset;}});
        }catch(Exception ignored){/* Expire old state instead of drawing stale appearances forever. */}finally{mc.execute(()->pending=false);}});
    }
    public static Look look(PlayerEntityRenderState state){
        var mc=MinecraftClient.getInstance();if(mc.world==null||state.invisible||state.spectator||System.currentTimeMillis()>validUntil)return null;
        var entity=mc.world.getEntityById(state.id);return entity==null?null:looks.get(entity.getUuidAsString().replace("-",""));
    }
    public static EmotePose pose(PlayerEntityRenderState state){
        var look=look(state);if(look==null||look.emote().isEmpty()||state.deathTime>0||state.sneaking||state.touchingWater||!state.isInPose(net.minecraft.entity.EntityPose.STANDING)||look.until()<System.currentTimeMillis()+offset)return null;
        return EmotePose.at(look.emote(),(System.currentTimeMillis()+offset-look.started())/1000.0);
    }
    public static void emote(String id,Consumer<String> feedback){
        if(!sharing()){feedback.accept("Enable player sharing in launcher Settings to share emotes.");return;}
        request("emote",SocialClient.body("emote",id),r->{feedback.accept(id.isEmpty()?"Emote stopped":"Emote playing — use F5 to see yourself");},feedback);
    }
}
