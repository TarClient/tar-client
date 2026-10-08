package dev.tarclient.mod;
import dev.tarclient.launcher.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import java.util.*;
import java.util.concurrent.*;
public final class CommunityBadges {
    private static final ExecutorService worker=Executors.newSingleThreadExecutor(r->Thread.ofVirtual().name("Tar community").unstarted(r));
    private static CommunityService.Connection connection;
    private static Map<String,String> players=Map.of();
    private static long validUntil,next,generation;
    private static boolean pending,present;
    private static String identity="";
    private static Object world;
    private static boolean sharing;
    private static long privacyCheck;
    public static String status="Join a world to connect your badge";
    private static MicrosoftAuth.Session session(){var mc=MinecraftClient.getInstance();var s=mc.getSession();return new MicrosoftAuth.Session(s.getUsername(),s.getUuidOrNull()==null?"":s.getUuidOrNull().toString().replace("-",""),s.getAccessToken(),Long.MAX_VALUE,mc.isDemo());}
    // Called only on the community worker, including rank-management requests.
    private static CommunityService.Connection connection(MicrosoftAuth.Session session)throws Exception {
        if(connection!=null&&!connection.uuid().equals(session.uuid())){
            try{CommunityService.leave(connection);}catch(Exception ignored){}
            connection=null;
        }
        if(connection==null||connection.expires()<System.currentTimeMillis()+60000)connection=CommunityService.connect(session);
        return connection;
    }
    public static void tick(){
        var mc=MinecraftClient.getInstance();var auth=session();
        if(!identity.equals(auth.uuid())||world!=mc.world){
            identity=auth.uuid();world=mc.world;generation++;players=Map.of();validUntil=0;next=0;
            status=mc.world==null?"Join a world to connect your badge":"Connecting your badge...";
        }
        if(System.currentTimeMillis()>=privacyCheck){
            privacyCheck=System.currentTimeMillis()+1000;boolean allowed=PrivacyPreferences.defaults().sharingEnabled();
            if(allowed!=sharing){sharing=allowed;generation++;players=Map.of();validUntil=0;next=0;}
        }
        if(!sharing)status="Badge sharing is off in launcher Settings";
        // All authenticated Tar players advertise their normal (or assigned) badge.
        // The module switch is a local display preference, never a rank requirement.
        boolean active=sharing&&mc.world!=null&&!auth.demo()&&auth.uuid().matches("[a-f0-9]{32}")&&CommunityService.configured();
        if(!active){
            players=Map.of();
            if(present&&!pending){pending=true;worker.submit(()->{
                try{if(connection!=null)CommunityService.leave(connection);}catch(Exception ignored){}
                finally{mc.execute(()->{present=false;pending=false;});}
            });}
            return;
        }
        if(System.currentTimeMillis()>validUntil)players=Map.of();
        if(pending||System.currentTimeMillis()<next)return;
        pending=true;next=System.currentTimeMillis()+30000;long requestGeneration=generation;
        var ids=new LinkedHashSet<String>();ids.add(auth.uuid());
        if(mc.getNetworkHandler()!=null)for(var entry:mc.getNetworkHandler().getPlayerList())ids.add(entry.getProfile().id().toString().replace("-",""));
        worker.submit(()->{
            try{
                var found=CommunityService.heartbeatPlayers(connection(auth),List.copyOf(ids));
                mc.execute(()->{
                    // Even a discarded response may have published presence. Ensure a
                    // subsequent disconnect tick removes it instead of forgetting it.
                    present=true;
                    if(generation==requestGeneration&&mc.world!=null&&PrivacyPreferences.defaults().sharingEnabled()){
                        players=found;validUntil=System.currentTimeMillis()+90000;status="Connected / badge shared automatically";
                    }
                });
            }catch(Exception e){connection=null;mc.execute(()->{if(generation==requestGeneration)status="Badge service unavailable; retrying...";});}
            finally{mc.execute(()->pending=false);}
        });
    }
    public static Text decorate(UUID uuid,Text name){String rank=players.get(uuid.toString().replace("-",""));if(rank==null||!TarClient.CONFIG.on("badges")||System.currentTimeMillis()>validUntil)return name;
        int color=switch(rank){case "partner"->0xC4A0FF;case "mod"->0x91C8FF;case "admin"->0xFF9C9C;default->0xFFFFFF;};
        var glyph=Text.literal("\uE000").setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("tarclient","badge"))).withColor(color));return name.copy().append(" ").append(glyph);
    }
    public static void manage(String name,String rank,java.util.function.Consumer<String> result){var mc=MinecraftClient.getInstance();var auth=session();worker.submit(()->{try{if(!CommunityService.owner(auth.uuid()))throw new java.io.IOException("Only Tarrecool can manage ranks.");var response=CommunityService.rank(connection(auth),name,rank);mc.execute(()->{next=0;result.accept(response.get("name").getAsString()+": "+response.get("rank").getAsString());});}catch(Exception e){mc.execute(()->result.accept(e.getMessage()));}});}
}
