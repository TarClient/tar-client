package dev.tarclient.mod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import net.minecraft.util.Identifier;
import net.minecraft.client.texture.*;
import net.minecraft.client.gl.RenderPipelines;
import static dev.tarclient.mod.TarClient.CONFIG;

public final class TarHud {
    public static final Map<String,Box> BOXES=new LinkedHashMap<>();
    public record Box(int x,int y,int w,int h) {public boolean contains(double px,double py){return px>=x&&py>=y&&px<=x+w&&py<=y+h;}}
    private static final Map<String,Integer> textureHashes=new HashMap<>();
    private static final Map<String,byte[]> textureBytes=new HashMap<>();
    private static final Map<String,int[]> textureSizes=new HashMap<>();
    public static void render(DrawContext c,boolean editing) {
        var mc=MinecraftClient.getInstance();if(mc.player==null||mc.options.hudHidden&&!editing)return;
        BOXES.clear();
        if(CONFIG.on("fps")) textPanel(c,"fps",List.of(mc.getCurrentFps()+" FPS"));
        if(CONFIG.on("ping")) {
            var entry=mc.getNetworkHandler()==null?null:mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            String text=mc.isInSingleplayer()?"Singleplayer":entry==null?"Ping unavailable":entry.getLatency()+" ms";
            textPanel(c,"ping",List.of(text));
        }
        if(CONFIG.on("armor")) armor(c);
        if(CONFIG.on("keys")) keys(c);
        if(CONFIG.on("clock"))textPanel(c,"clock",List.of(LocalTime.now().format(DateTimeFormatter.ofPattern(CONFIG.bool("clock","seconds")?"h:mm:ss a":"h:mm a",Locale.US))));
        if(CONFIG.on("coordinates")){
            var pos=mc.player.getBlockPos();var lines=new ArrayList<String>();lines.add(StreamerMode.active()?"XYZ  "+StreamerMode.position().block()+" [Streamer]":"XYZ  "+pos.getX()+"  "+pos.getY()+"  "+pos.getZ());
            if(CONFIG.bool("coordinates","dimension"))lines.add(mc.world.getRegistryKey().getValue().getPath());textPanel(c,"coordinates",lines);
        }
        if(CONFIG.on("inventory"))inventory(c);
        if(CONFIG.on("saturation"))textPanel(c,"saturation",List.of(String.format(Locale.ROOT,"Saturation%s  %.1f",mc.isInSingleplayer()?"":" (estimate)",ClientFeatures.saturation)));
        if(CONFIG.on("reach")&&(editing||System.currentTimeMillis()-ClientFeatures.lastAttack<CONFIG.number("reach","seconds")*1000))textPanel(c,"reach",List.of(String.format(Locale.ROOT,"Last attack  %.2f blocks",ClientFeatures.reach)));
        if(CONFIG.on("server"))server(c);
        if(editing)for(var e:BOXES.entrySet()){var b=e.getValue();c.drawStrokedRectangle(b.x-1,b.y-1,b.w+2,b.h+2,0xFFA5F078);}
    }
    private static void begin(DrawContext c,String id,int w,int h) {
        float scale=CONFIG.f(id,"scale");int width=Math.round(w*scale),height=Math.round(h*scale);
        int x=Math.round((c.getScaledWindowWidth()-width)*CONFIG.f(id,"x")/100),y=Math.round((c.getScaledWindowHeight()-height)*CONFIG.f(id,"y")/100);
        x=Math.max(0,x);y=Math.max(0,y);BOXES.put(id,new Box(x,y,width,height));
        c.getMatrices().pushMatrix();c.getMatrices().translate(x,y);c.getMatrices().scale(scale,scale);
        if(CONFIG.bool(id,"showBackground")){int radius=CONFIG.i(id,"radius");rounded(c,0,0,w,h,radius,CONFIG.color(id,"background"));}
    }
    private static void end(DrawContext c){c.getMatrices().popMatrix();}
    private static void textPanel(DrawContext c,String id,List<String> lines) {
        var font=MinecraftClient.getInstance().textRenderer;int w=lines.stream().mapToInt(font::getWidth).max().orElse(60)+12;
        begin(c,id,w,lines.size()*13+8);int y=5;for(String s:lines){c.drawTextWithShadow(font,s,6,y,CONFIG.color(id,"color"));y+=13;}end(c);
    }
    private static void armor(DrawContext c) {
        var mc=MinecraftClient.getInstance();boolean horizontal=CONFIG.bool("armor","horizontal"),text=CONFIG.bool("armor","text"),bar=CONFIG.bool("armor","bar");
        var stacks=Arrays.stream(TarClient.ARMOR).map(mc.player::getEquippedStack).filter(stack->!stack.isEmpty()||CONFIG.bool("armor","empty")).toList();
        if(stacks.isEmpty())stacks=List.of(net.minecraft.item.ItemStack.EMPTY);
        int icon=CONFIG.i("armor","iconSize"),barWidth=CONFIG.i("armor","barWidth"),textWidth=0;
        if(text)for(var stack:stacks)textWidth=Math.max(textWidth,mc.textRenderer.getWidth(durabilityText(stack)));
        int infoWidth=Math.max(textWidth,bar?barWidth:0),infoHeight=(text?10:0)+(bar?5:0);
        int cellWidth=horizontal?Math.max(icon,infoWidth):icon+(infoWidth>0?6+infoWidth:0);
        int cellHeight=horizontal?icon+(infoHeight>0?3+infoHeight:0):Math.max(icon,infoHeight);
        var layout=new dev.tarclient.config.ArmorLayout(stacks.size(),horizontal,CONFIG.bool("armor","reverse"),cellWidth,cellHeight,CONFIG.i("armor","spacing"));
        begin(c,"armor",layout.width(),layout.height());
        for(int i=0;i<stacks.size();i++){
            var stack=stacks.get(i);int x=layout.x(i),y=layout.y(i);
            if(!stack.isEmpty()){
                c.getMatrices().pushMatrix();c.getMatrices().translate(x,y);c.getMatrices().scale(icon/16f,icon/16f);c.drawItem(stack,0,0);c.getMatrices().popMatrix();
                double pct=stack.isDamageable()?Math.max(0,stack.getMaxDamage()-stack.getDamage())*100.0/stack.getMaxDamage():100;
                int color=CONFIG.color("armor","color");
                if(CONFIG.bool("armor","durabilityColors")&&stack.isDamageable())color=pct<=CONFIG.number("armor","threshold")?0xFFFF7878:pct<40?0xFFFFCD70:0xFFA5F078;
                int tx=horizontal?x:x+icon+6,ty=horizontal?y+icon+3:y;
                if(text)c.drawTextWithShadow(mc.textRenderer,durabilityText(stack),tx,ty,color);
                if(bar&&stack.isDamageable()){int by=ty+(text?12:2);c.fill(tx,by,tx+barWidth,by+2,0x60414D60);c.fill(tx,by,tx+(int)(barWidth*pct/100),by+2,color);}
            }else if(CONFIG.bool("armor","empty"))c.drawTextWithShadow(mc.textRenderer,"â€”",x+icon/2-3,y+icon/2-4,0xFF778397);
        }end(c);
    }
    private static String durabilityText(net.minecraft.item.ItemStack stack){
        if(!stack.isDamageable())return "â€”";
        int remaining=Math.max(0,stack.getMaxDamage()-stack.getDamage());
        return CONFIG.bool("armor","percent")?Math.round(remaining*100.0/stack.getMaxDamage())+"%":Integer.toString(remaining);
    }
    private static void inventory(DrawContext c){
        var mc=MinecraftClient.getInstance();boolean hotbar=CONFIG.bool("inventory","hotbar");begin(c,"inventory",168,hotbar?78:58);
        for(int i=0;i<(hotbar?36:27);i++){int slot=i<27?i+9:i-27,x=3+i%9*18,y=3+i/9*18;var stack=mc.player.getInventory().getStack(slot);c.drawItem(stack,x,y);if(CONFIG.bool("inventory","counts"))c.drawStackOverlay(mc.textRenderer,stack,x,y);}
        end(c);
    }
    private static void server(DrawContext c){
        var mc=MinecraftClient.getInstance();var info=mc.getCurrentServerEntry();
        if(info==null){textPanel(c,"server",List.of(mc.isInSingleplayer()?"Singleplayer":"Server unavailable"));return;}
        boolean icon=CONFIG.bool("server","icon");int offset=icon?38:6;
        int w=Math.max(mc.textRenderer.getWidth(info.address),CONFIG.bool("server","name")?mc.textRenderer.getWidth(info.name):0)+offset+8;
        begin(c,"server",w,36);if(icon)picture(c,"server",info.getFavicon(),4,4,28,3);
        if(CONFIG.bool("server","name")){c.drawTextWithShadow(mc.textRenderer,info.name,offset,5,CONFIG.color("server","color"));c.drawTextWithShadow(mc.textRenderer,info.address,offset,20,CONFIG.color("server","color"));}
        else c.drawTextWithShadow(mc.textRenderer,info.address,offset,13,CONFIG.color("server","color"));end(c);
    }
    static void rounded(DrawContext c,int x,int y,int w,int h,int r,int color){
        r=Math.clamp(r,0,Math.min(w,h)/2);for(int row=0;row<h;row++){int d=row<r?r-row:row>=h-r?row-(h-r-1):0;int inset=d==0?0:(int)Math.ceil(r-Math.sqrt(Math.max(0,r*r-d*d)));c.fill(x+inset,y+row,x+w-inset,y+row+1,color);}
    }
    private static void picture(DrawContext c,String key,byte[] bytes,int x,int y,int size,int radius){
        var mc=MinecraftClient.getInstance();var id=Identifier.of("tarclient","dynamic/"+key);
        if(!textureBytes.containsKey(key)||textureBytes.get(key)!=bytes){
        textureBytes.put(key,bytes);int hash=Arrays.hashCode(bytes);
        if(!Objects.equals(textureHashes.get(key),hash)){
            textureHashes.put(key,hash);textureSizes.remove(key);mc.getTextureManager().destroyTexture(id);
            if(bytes!=null&&bytes.length>0&&bytes.length<=1_048_576)try{var image=NativeImage.read(bytes);if(image.getWidth()>1024||image.getHeight()>1024){image.close();throw new IllegalArgumentException("Image too large");}textureSizes.put(key,new int[]{image.getWidth(),image.getHeight()});mc.getTextureManager().registerTexture(id,new NativeImageBackedTexture(()->"Tar "+key,image));}catch(Exception ignored){}
        }
        }
        var dimensions=textureSizes.get(key);if(dimensions==null){rounded(c,x,y,size,size,radius,0xFF384859);return;}
        // Clip each scanline to the chosen shape without changing the artwork's texture.
        for(int row=0;row<size;row++){int d=row<radius?radius-row:row>=size-radius?row-(size-radius-1):0;int inset=d==0?0:(int)Math.ceil(radius-Math.sqrt(Math.max(0,radius*radius-d*d)));
            c.enableScissor(x+inset,y+row,x+size-inset,y+row+1);c.drawTexture(RenderPipelines.GUI_TEXTURED,id,x,y,0,0,size,size,dimensions[0],dimensions[1],dimensions[0],dimensions[1]);c.disableScissor();}
    }
    private static void keys(DrawContext c) {
        var mc=MinecraftClient.getInstance();boolean mouse=CONFIG.bool("keys","mouse");begin(c,"keys",86,mouse?106:76);
        key(c,mc.options.forwardKey,30,3,26);key(c,mc.options.leftKey,2,28,26);key(c,mc.options.backKey,30,28,26);key(c,mc.options.rightKey,58,28,26);key(c,mc.options.jumpKey,2,53,82);
        if(mouse){long h=mc.getWindow().getHandle();mouse(c,2,78,"LMB",GLFW.glfwGetMouseButton(h,0)==GLFW.GLFW_PRESS,TarClient.LEFT_CLICKS.size());mouse(c,44,78,"RMB",GLFW.glfwGetMouseButton(h,1)==GLFW.GLFW_PRESS,TarClient.RIGHT_CLICKS.size());}end(c);
    }
    private static void key(DrawContext c,KeyBinding key,int x,int y,int width) {String name=key.getBoundKeyLocalizedText().getString();if(name.length()>9)name=name.substring(0,9);cell(c,x,y,width,21,name,key.isPressed());}
    private static void mouse(DrawContext c,int x,int y,String label,boolean pressed,int cps){cell(c,x,y,40,25,label,pressed);if(CONFIG.bool("keys","cps"))c.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,cps+" CPS",x+4,y+14,CONFIG.color("keys","color"));}
    private static void cell(DrawContext c,int x,int y,int w,int h,String s,boolean pressed){if(CONFIG.bool("keys","showBackground"))rounded(c,x,y,w,h,3,pressed?CONFIG.color("keys","pressed"):0xBB2B3342);var font=MinecraftClient.getInstance().textRenderer;c.drawTextWithShadow(font,s,x+(w-font.getWidth(s))/2,y+4,pressed?(CONFIG.bool("keys","showBackground")?0xFF111820:CONFIG.color("keys","pressed")):CONFIG.color("keys","color"));}
    public static void crosshair(DrawContext c) {
        var mc=MinecraftClient.getInstance();if(mc.player==null||mc.player.isSpectator()||!mc.options.getPerspective().isFirstPerson()&&!CONFIG.bool("crosshair","thirdPerson"))return;
        if(CONFIG.bool("crosshair","grid")){
            String pattern=dev.tarclient.config.CrosshairPattern.normalize(CONFIG.text("crosshair","pattern"));int scale=CONFIG.i("crosshair","pixelScale"),cx=c.getScaledWindowWidth()/2-7*scale,cy=c.getScaledWindowHeight()/2-7*scale;
            if(CONFIG.bool("crosshair","outline"))for(int py=0;py<15;py++)for(int px=0;px<15;px++)if(dev.tarclient.config.CrosshairPattern.pixel(pattern,px,py))c.fill(cx+px*scale-1,cy+py*scale-1,cx+(px+1)*scale+1,cy+(py+1)*scale+1,0xFF000000);
            for(int py=0;py<15;py++)for(int px=0;px<15;px++)if(dev.tarclient.config.CrosshairPattern.pixel(pattern,px,py))c.fill(cx+px*scale,cy+py*scale,cx+(px+1)*scale,cy+(py+1)*scale,CONFIG.color("crosshair","color"));
            return;
        }
        int x=c.getScaledWindowWidth()/2,y=c.getScaledWindowHeight()/2,s=CONFIG.i("crosshair","size"),g=CONFIG.i("crosshair","gap"),t=CONFIG.i("crosshair","thickness"),o=t/2,color=CONFIG.color("crosshair","color");
        int[][] rects={{x-g-s,y-o,x-g,y-o+t},{x+g+1,y-o,x+g+s+1,y-o+t},{x-o,y-g-s,x-o+t,y-g},{x-o,y+g+1,x-o+t,y+g+s+1}};
        if(CONFIG.bool("crosshair","outline"))for(var r:rects)c.fill(r[0]-1,r[1]-1,r[2]+1,r[3]+1,0xFF000000);
        for(var r:rects)c.fill(r[0],r[1],r[2],r[3],color);
        if(CONFIG.bool("crosshair","dot")){if(CONFIG.bool("crosshair","outline"))c.fill(x-o-1,y-o-1,x-o+t+1,y-o+t+1,0xFF000000);c.fill(x-o,y-o,x-o+t,y-o+t,color);}
    }
}
