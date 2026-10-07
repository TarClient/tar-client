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
    private static long validUntil,next;
    private static boolean pending,present;
    private static String identity="";
    public static String status="Community not connected";
    private static MicrosoftAuth.Session session(){var mc=MinecraftClient.getInstance();var s=mc.getSession();return new MicrosoftAuth.Session(s.getUsername(),s.getUuidOrNull()==null?"":s.getUuidOrNull().toString().replace("-",""),s.getAccessToken(),Long.MAX_VALUE,mc.isDemo());}
    private static CommunityService.Connection connection(MicrosoftAuth.Session session)throws Exception {if(connection==null||!connection.uuid().equals(session.uuid())||connection.expires()<System.currentTimeMillis()+60000)connection=CommunityService.connect(session);return connection;}
    public static void tick(){var mc=MinecraftClient.getInstance();String id=session().uuid();if(!identity.equals(id)){identity=id;players=Map.of();validUntil=0;next=0;}
        boolean enabled=TarClient.CONFIG.on("badges")&&mc.world!=null&&!mc.isDemo()&&CommunityService.configured();
        if(!enabled){players=Map.of();if(present&&!pending){present=false;pending=true;worker.submit(()->{try{if(connection!=null)CommunityService.leave(connection);}catch(Exception ignored){}finally{mc.execute(()->pending=false);}});}return;}
        if(System.currentTimeMillis()>validUntil)players=Map.of();
        if(pending||System.currentTimeMillis()<next)return;pending=true;next=System.currentTimeMillis()+30000;
        var auth=session();var ids=new ArrayList<String>();ids.add(auth.uuid());if(mc.getNetworkHandler()!=null)for(var entry:mc.getNetworkHandler().getPlayerList()){String uuid=entry.getProfile().id().toString().replace("-","");if(ids.size()<100&&!ids.contains(uuid))ids.add(uuid);}
        worker.submit(()->{try{var result=CommunityService.heartbeat(connection(auth),ids);Map<String,String> found=new HashMap<>();for(var entry:result.getAsJsonObject("players").entrySet())if(ids.contains(entry.getKey())&&Set.of("normal","partner","mod","admin").contains(entry.getValue().getAsString()))found.put(entry.getKey(),entry.getValue().getAsString());
            mc.execute(()->{if(identity.equals(auth.uuid())&&TarClient.CONFIG.on("badges")&&mc.world!=null){players=Map.copyOf(found);validUntil=System.currentTimeMillis()+90000;present=true;status="Connected";}});
        }catch(Exception e){connection=null;mc.execute(()->status="Community unavailable; badges expire automatically.");}finally{mc.execute(()->pending=false);}});
    }
    public static Text decorate(UUID uuid,Text name){String rank=players.get(uuid.toString().replace("-",""));if(rank==null||!TarClient.CONFIG.on("badges")||System.currentTimeMillis()>validUntil)return name;
        int color=switch(rank){case "partner"->0xC4A0FF;case "mod"->0x91C8FF;case "admin"->0xFF9C9C;default->0xFFFFFF;};
        var glyph=Text.literal("\uE000").setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("tarclient","badge"))).withColor(color));return name.copy().append(" ").append(glyph);
    }
    public static void manage(String name,String rank,java.util.function.Consumer<String> result){var mc=MinecraftClient.getInstance();var auth=session();worker.submit(()->{try{if(!CommunityService.owner(auth.uuid()))throw new java.io.IOException("Only Tarrecool can manage ranks.");var response=CommunityService.rank(connection(auth),name,rank);mc.execute(()->{next=0;result.accept(response.get("name").getAsString()+": "+response.get("rank").getAsString());});}catch(Exception e){mc.execute(()->result.accept(e.getMessage()));}});}
}
