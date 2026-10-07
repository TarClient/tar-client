package dev.tarclient.mod;
import dev.tarclient.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.TntEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import java.util.*;
import static dev.tarclient.mod.TarClient.CONFIG;
public final class ExtraHud {
    private static final ArrayDeque<Double> rates=new ArrayDeque<>();
    private static long lastPacket,lastWorldTick;
    private static Object world;
    private record Point(double x,double y,long time){}
    private static final ArrayDeque<Point> trail=new ArrayDeque<>();
    private static double mouseX,mouseY;
    public static void tick(){Object current=MinecraftClient.getInstance().world;if(current!=world){world=current;rates.clear();lastPacket=0;trail.clear();}}
    public static void time(long tick){long now=System.nanoTime();if(lastPacket!=0&&tick>lastWorldTick){double dt=(now-lastPacket)/1e9;if(dt>0.05&&dt<120){rates.addLast(Math.clamp((tick-lastWorldTick)/dt,0,20));while(rates.size()>CONFIG.i("tps","samples"))rates.removeFirst();}}lastPacket=now;lastWorldTick=tick;}
    public static String tps(){if(rates.isEmpty()||System.nanoTime()-lastPacket>15_000_000_000L)return "TPS: waiting for server";return String.format(Locale.ROOT,"TPS (estimate)  %.1f",rates.stream().mapToDouble(Double::doubleValue).average().orElse(0));}
    public static void mouse(double dx,double dy){if(!CONFIG.on("mousetracer"))return;long now=System.nanoTime();if(trail.isEmpty()||now-trail.peekLast().time>1_000_000_000L){mouseX=0;mouseY=0;trail.clear();}
        double w=CONFIG.number("mousetracer","width")/2-4,h=CONFIG.number("mousetracer","height")/2-4,s=CONFIG.number("mousetracer","sensitivity");
        mouseX=Math.clamp(mouseX+dx*s,-w,w);mouseY=Math.clamp(mouseY+dy*s,-h,h);trail.addLast(new Point(mouseX,mouseY,now));while(trail.size()>512)trail.removeFirst();}
    public static void render(DrawContext c,boolean editing){var mc=MinecraftClient.getInstance();
        if(CONFIG.on("tps"))TarHud.textPanel(c,"tps",List.of(tps()));
        if(CONFIG.on("text")){String value=CONFIG.text("text","text").replace("\\n","\n");TarHud.textPanel(c,"text",Arrays.stream(value.split("\n",-1)).limit(12).map(line->mc.textRenderer.trimToWidth(line,600)).toList());}
        if(CONFIG.on("itemcounter")){
            var lines=new ArrayList<String>();for(String item:ClientConfig.itemIds(CONFIG.text("itemcounter","items")).stream().limit(24).toList()){
                Identifier id=Identifier.tryParse(item);if(id==null||!Registries.ITEM.containsId(id))continue;var type=Registries.ITEM.get(id);int count=0;
                for(int slot=0;slot<mc.player.getInventory().size();slot++){var stack=mc.player.getInventory().getStack(slot);if(stack.isOf(type))count+=stack.getCount();}
                if(count>0||!CONFIG.bool("itemcounter","hideZero"))lines.add(type.getName().getString()+": "+count);
            }if(lines.isEmpty())lines.add(editing?"Item counter":"No selected items");TarHud.textPanel(c,"itemcounter",lines);
        }
        if(CONFIG.on("tnttimer")){
            var start=mc.player.getCameraPosVec(1);var end=start.add(mc.player.getRotationVec(1).multiply(CONFIG.number("tnttimer","range")));
            var block=mc.world.raycast(new RaycastContext(start,end,RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,mc.player));double nearest=block.getType()==HitResult.Type.MISS?start.squaredDistanceTo(end):start.squaredDistanceTo(block.getPos());
            TntEntity selected=null;for(var tnt:mc.world.getEntitiesByClass(TntEntity.class,mc.player.getBoundingBox().stretch(end.subtract(start)).expand(1),e->!e.isRemoved())){
                var hit=tnt.getBoundingBox().expand(0.05).raycast(start,end);if(hit.isPresent()&&start.squaredDistanceTo(hit.get())<nearest){nearest=start.squaredDistanceTo(hit.get());selected=tnt;}
            }if(selected!=null||editing)TarHud.textPanel(c,"tnttimer",List.of(selected==null?"TNT  4.0s":String.format(Locale.ROOT,"TNT  %.1fs",Math.max(0,selected.getFuse())/20.0)));
        }
        if(CONFIG.on("mousetracer")){
            int w=CONFIG.i("mousetracer","width"),h=CONFIG.i("mousetracer","height");TarHud.begin(c,"mousetracer",w,h);
            long now=System.nanoTime(),life=(long)(CONFIG.number("mousetracer","seconds")*1e9);while(!trail.isEmpty()&&now-trail.peekFirst().time>life)trail.removeFirst();
            Point previous=null;for(Point point:trail){if(previous!=null){int color=CONFIG.color("mousetracer","color"),alpha=(int)(255*(1-(now-point.time)/(double)life));line(c,w/2+previous.x,h/2+previous.y,w/2+point.x,h/2+point.y,(color&0xffffff)|(Math.clamp(alpha,0,255)<<24));}previous=point;}
            c.fill(w/2-1,h/2-1,w/2+1,h/2+1,0x887F8B9F);if(previous!=null)c.fill((int)(w/2+previous.x)-1,(int)(h/2+previous.y)-1,(int)(w/2+previous.x)+2,(int)(h/2+previous.y)+2,CONFIG.color("mousetracer","color"));TarHud.end(c);
        }
    }
    private static void line(DrawContext c,double x,double y,double ex,double ey,int color){int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(ex-x),Math.abs(ey-y))));for(int i=0;i<=steps;i++){int px=(int)Math.round(x+(ex-x)*i/steps),py=(int)Math.round(y+(ey-y)*i/steps);c.fill(px,py,px+1,py+1,color);}}
}
