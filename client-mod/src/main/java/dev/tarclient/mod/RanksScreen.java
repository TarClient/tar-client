package dev.tarclient.mod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
public final class RanksScreen extends Screen {
    private final Screen parent;private String name="",status="Only Tarrecool can grant or remove ranks.";private boolean busy;
    public RanksScreen(Screen parent){super(Text.literal("Tar ranks"));this.parent=parent;}
    protected void init(){int w=Math.min(400,width-24),x=(width-w)/2;
        var field=new TextFieldWidget(textRenderer,x,48,w,22,Text.literal("Minecraft username"));field.setMaxLength(16);field.setPlaceholder(Text.literal("Minecraft username"));field.setText(name);field.setChangedListener(v->name=v);addDrawableChild(field);
        String[] names={"Normal (white / remove rank)","Partner (light purple)","Mod (light blue)","Admin (light red)"},ranks={"normal","partner","mod","admin"};
        names[0]="Normal / remove rank";
        for(int i=0;i<4;i++){String rank=ranks[i];var button=new TarButton(x+(i%2)*(w/2+3),82+(i/2)*27,w/2-3,22,names[i],b->{if(!name.matches("[A-Za-z0-9_]{1,16}")){status="Enter a valid Minecraft username.";return;}busy=true;status="Updating rank...";clearAndInit();CommunityBadges.manage(name,rank,message->{busy=false;status=message;clearAndInit();});});button.active=!busy;addDrawableChild(button);}
        addDrawableChild(new TarButton(width/2-45,height-28,90,20,"Done",b->close()));
    }
    public void render(DrawContext c,int x,int y,float delta){c.fill(0,0,width,height,UiTheme.color(0xF0101720));c.drawCenteredTextWithShadow(textRenderer,"MANAGE TAR RANKS",width/2,22,UiTheme.color(0xFFE8EDF5));int lineY=143;for(var line:textRenderer.wrapLines(Text.literal(status),width-24)){if(lineY>height-40)break;c.drawTextWithShadow(textRenderer,line,12,lineY,UiTheme.color(0xFFE8EDF5));lineY+=10;}super.render(c,x,y,delta);}
    public void close(){client.setScreen(parent);}public boolean shouldPause(){return false;}
}
