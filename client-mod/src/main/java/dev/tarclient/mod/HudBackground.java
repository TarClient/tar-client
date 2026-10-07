package dev.tarclient.mod;
import net.minecraft.client.gui.DrawContext;
import static dev.tarclient.mod.TarClient.CONFIG;
public final class HudBackground {
    public static void draw(DrawContext c,String id,int x,int y,int w,int h){
        if(!CONFIG.bool(id,"showBackground"))return;
        int color=CONFIG.color(id,"background"),radius=CONFIG.i(id,"radius");
        switch(CONFIG.text(id,"backgroundStyle")){
            case "solid"->c.fill(x,y,x+w,y+h,color);
            case "gradient"->{int end=CONFIG.color(id,"backgroundEnd");for(int row=0;row<h;row++){double t=row/(double)Math.max(1,h-1);int mixed=0;for(int shift:new int[]{0,8,16,24})mixed|=((int)Math.round(((color>>>shift)&255)*(1-t)+((end>>>shift)&255)*t))<<shift;c.fill(x,y+row,x+w,y+row+1,mixed);}}
            case "outline"->c.drawStrokedRectangle(x,y,w,h,color);
            default->TarHud.rounded(c,x,y,w,h,radius,color);
        }
    }
}
