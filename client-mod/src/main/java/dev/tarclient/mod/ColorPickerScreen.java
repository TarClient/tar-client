package dev.tarclient.mod;

import dev.tarclient.config.ColorPickerModel;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFW;

/** A reusable gradient-square color picker. Apply commits; Escape/Cancel leave settings untouched. */
public final class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final ColorPickerModel model;
    private final boolean alphaAllowed;
    private final String defaultColor;
    private final Consumer<String> save;
    private TextFieldWidget hex;
    private TarButton apply;
    private int left,top,panelWidth,planeY,planeHeight,hueY,footerY;
    private boolean syncing,valid=true;
    public ColorPickerScreen(Screen parent,String label,String initial,String defaultColor,boolean alphaAllowed,Consumer<String> save){
        super(Text.literal(label));this.parent=parent;this.defaultColor=defaultColor;this.alphaAllowed=alphaAllowed;this.save=save;model=new ColorPickerModel(ColorPickerModel.valid(initial,alphaAllowed)?initial:defaultColor,alphaAllowed);
    }
    @Override protected void init(){
        panelWidth=Math.min(360,width-24);planeHeight=Math.max(90,Math.min(142,height-(alphaAllowed?150:122)));
        left=(width-panelWidth)/2;top=Math.max(4,(height-planeHeight-(alphaAllowed?142:114))/2);planeY=top+22;hueY=planeY+planeHeight+14;footerY=hueY+(alphaAllowed?56:28);
        addDrawableChild(new Area(left,planeY,panelWidth-94,planeHeight,0));
        addDrawableChild(new Area(left,hueY,panelWidth,14,1));
        if(alphaAllowed)addDrawableChild(new Area(left,hueY+26,panelWidth,14,2));
        hex=new TextFieldWidget(textRenderer,left+panelWidth-84,planeY+58,84,20,Text.literal(alphaAllowed?"ARGB hex":"RGB hex"));
        hex.setMaxLength(alphaAllowed?9:7);hex.setEditableColor(0xFFFFFFFF);hex.setText(model.hex());
        hex.setChangedListener(v->{if(!syncing){valid=model.setHex(v);apply.active=valid;}});addDrawableChild(hex);
        int bw=(panelWidth-12)/3;
        addDrawableChild(new TarButton(left,footerY,bw,22,"Default",b->{model.setHex(defaultColor);syncHex();}));
        addDrawableChild(new TarButton(left+bw+6,footerY,bw,22,"Cancel",b->close()));
        apply=addDrawableChild(new TarButton(left+2*(bw+6),footerY,bw,22,"Apply",b->{save.accept(model.hex());close();},()->true));
        valid=true;
    }
    private void syncHex(){syncing=true;hex.setText(model.hex());syncing=false;valid=true;apply.active=true;}
    private static void checker(DrawContext c,int x,int y,int w,int h){for(int row=0;row<h;row+=6)for(int col=0;col<w;col+=6)c.fill(x+col,y+row,x+Math.min(w,col+6),y+Math.min(h,row+6),((row/6+col/6)%2==0)?0xFFAAAAAA:0xFF555555);}
    @Override public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,UiTheme.color(0xFF101720));
        c.drawCenteredTextWithShadow(textRenderer,textRenderer.trimToWidth(title.getString(),panelWidth),width/2,top+8,UiTheme.color(0xFFF0F5FA));
        int previewX=left+panelWidth-84;checker(c,previewX,planeY,84,36);c.fill(previewX,planeY,previewX+84,planeY+36,model.argb());
        c.drawTextWithShadow(textRenderer,alphaAllowed?"ARGB hex":"RGB hex",previewX,planeY+45,UiTheme.color(0xFF9DADBF));
        if(!valid)c.drawTextWithShadow(textRenderer,"Invalid hex",previewX,planeY+82,0xFFFF7777);
        c.drawTextWithShadow(textRenderer,"Hue",left,hueY-11,UiTheme.color(0xFFEAF0F6));
        if(alphaAllowed)c.drawTextWithShadow(textRenderer,"Opacity: "+Math.round(model.alpha()*100)+"%",left,hueY+15,UiTheme.color(0xFFEAF0F6));
        c.drawCenteredTextWithShadow(textRenderer,textRenderer.trimToWidth("Drag to pick. Tab + arrow keys also work.",panelWidth),width/2,footerY+29,UiTheme.color(0xFF9DADBF));
        super.render(c,mx,my,delta);
    }
    @Override public void close(){client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
    private final class Area extends ClickableWidget {
        private final int kind;
        Area(int x,int y,int w,int h,int kind){super(x,y,w,h,Text.literal(kind==0?"Saturation and brightness":kind==1?"Hue":"Opacity"));this.kind=kind;}
        private void pick(double x,double y){
            double position=(x-getX())/Math.max(1,getWidth()-1);
            if(kind==0)model.saturationValue(position,1-(y-getY())/Math.max(1,getHeight()-1));
            else if(kind==1)model.hue(position);else model.alpha(position);
            syncHex();
        }
        @Override public void onClick(Click click,boolean doubled){pick(click.x(),click.y());}
        @Override protected void onDrag(Click click,double dx,double dy){pick(click.x(),click.y());}
        @Override public boolean keyPressed(KeyInput key){
            double step=(key.modifiers()&GLFW.GLFW_MOD_SHIFT)!=0?0.1:0.01;
            int k=key.key();if(k!=GLFW.GLFW_KEY_LEFT&&k!=GLFW.GLFW_KEY_RIGHT&&k!=GLFW.GLFW_KEY_UP&&k!=GLFW.GLFW_KEY_DOWN)return false;
            if(kind==0)model.saturationValue(model.saturation()+(k==GLFW.GLFW_KEY_LEFT?-step:k==GLFW.GLFW_KEY_RIGHT?step:0),model.value()+(k==GLFW.GLFW_KEY_UP?step:k==GLFW.GLFW_KEY_DOWN?-step:0));
            else{double d=k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_DOWN?-step:step;if(kind==1)model.hue(model.hue()+d);else model.alpha(model.alpha()+d);}
            syncHex();return true;
        }
        @Override protected void renderWidget(DrawContext c,int mx,int my,float delta){
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();
            if(kind==2)checker(c,x,y,w,h);
            for(int col=0;col<w;col++){
                double position=col/(double)Math.max(1,w-1);
                if(kind==0)c.fillGradient(x+col,y,x+col+1,y+h,0xFF000000|ColorPickerModel.rgb(model.hue(),position,1),0xFF000000);
                else c.fill(x+col,y,x+col+1,y+h,kind==1?0xFF000000|ColorPickerModel.rgb(position,1,1):((int)Math.round(position*255)<<24)|(model.argb()&0xFFFFFF));
            }
            double fraction=kind==0?model.saturation():kind==1?model.hue():model.alpha();
            int px=x+(int)Math.round(fraction*(w-1));
            if(kind==0){int py=y+(int)Math.round((1-model.value())*(h-1));c.drawStrokedRectangle(px-3,py-3,7,7,0xFF000000);c.drawStrokedRectangle(px-2,py-2,5,5,0xFFFFFFFF);}
            else{c.fill(px-2,y-1,px+3,y+h+1,0xFF000000);c.fill(px-1,y-1,px+2,y+h+1,0xFFFFFFFF);}
            if(isFocused())c.drawStrokedRectangle(x-2,y-2,w+4,h+4,0xFFFFFFFF);
        }
        @Override protected void appendClickableNarrations(NarrationMessageBuilder builder){setMessage(Text.literal((kind==0?"Saturation and brightness":kind==1?"Hue":"Opacity")+", selected color "+model.hex()+". Use arrow keys to adjust."));appendDefaultNarrations(builder);}
    }
}
