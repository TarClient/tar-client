package dev.tarclient.mod;

import dev.tarclient.config.CrosshairPattern;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import static dev.tarclient.mod.TarClient.CONFIG;

public final class CrosshairEditorScreen extends Screen {
    private final Screen parent;
    private String pattern;
    private int left,top,cell;
    private boolean drawing,ink;
    public CrosshairEditorScreen(Screen parent){super(Text.literal("Draw crosshair"));this.parent=parent;pattern=CrosshairPattern.normalize(CONFIG.text("crosshair","pattern"));}
    @Override protected void init(){
        cell=Math.max(4,Math.min(14,(height-104)/15));left=(width-cell*15)/2;top=48;
        int y=top+cell*15+9;
        addDrawableChild(new TarButton(width/2-122,y,76,20,"Clear",b->pattern="0".repeat(225)));
        addDrawableChild(new TarButton(width/2-40,y,80,20,"+ Preset",b->pattern=CrosshairPattern.vanilla()));
        addDrawableChild(new TarButton(width/2+46,y,76,20,"Save",b->{CONFIG.set("crosshair","pattern",pattern);CONFIG.set("crosshair","vanilla",false);CONFIG.set("crosshair","grid",true);CONFIG.set("crosshair","enabled",true);TarClient.save();close();}));
    }
    private boolean inside(Click click){return click.x()>=left&&click.y()>=top&&click.x()<left+15*cell&&click.y()<top+15*cell;}
    private void paint(Click click){pattern=CrosshairPattern.paint(pattern,(int)(click.x()-left)/cell,(int)(click.y()-top)/cell,ink);}
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(inside(click)&&(click.button()==0||click.button()==1)){int x=(int)(click.x()-left)/cell,y=(int)(click.y()-top)/cell;ink=click.button()==0&&!CrosshairPattern.pixel(pattern,x,y);drawing=true;paint(click);return true;}
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){if(drawing){if(inside(click))paint(click);return true;}return super.mouseDragged(click,dx,dy);}
    @Override public boolean mouseReleased(Click click){if(drawing){drawing=false;return true;}return super.mouseReleased(click);}
    @Override public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,0xF0101720);c.drawCenteredTextWithShadow(textRenderer,"DRAW YOUR CROSSHAIR",width/2,12,0xFFB9F47A);
        c.drawCenteredTextWithShadow(textRenderer,"Click / drag to paint. Right-click to erase. Escape cancels.",width/2,29,0xFF9DADBF);
        for(int y=0;y<15;y++)for(int x=0;x<15;x++){int color=CrosshairPattern.pixel(pattern,x,y)?CONFIG.color("crosshair","color"):((x+y)%2==0?0xFF273240:0xFF344150);c.fill(left+x*cell,top+y*cell,left+(x+1)*cell-1,top+(y+1)*cell-1,color);}
        int px=left+15*cell+12,py=top+6;
        if(px+32<width){c.fill(px-4,py-4,px+34,py+34,0xFF52684C);for(int y=0;y<15;y++)for(int x=0;x<15;x++)if(CrosshairPattern.pixel(pattern,x,y))c.fill(px+x*2,py+y*2,px+x*2+2,py+y*2+2,CONFIG.color("crosshair","color"));}
        super.render(c,mx,my,delta);
    }
    @Override public void close(){client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
}
