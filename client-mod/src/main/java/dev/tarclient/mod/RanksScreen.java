package dev.tarclient.mod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import dev.tarclient.launcher.BadgeRank;
import dev.tarclient.launcher.CommunityService;
public final class RanksScreen extends Screen {
    private final Screen parent;private String name="",status="";private boolean busy;private int statusY;
    public RanksScreen(Screen parent){super(Text.literal("Tar ranks"));this.parent=parent;}
    protected void init(){int w=Math.min(400,width-24),x=(width-w)/2;
        var field=new TextFieldWidget(textRenderer,x,48,w,22,Text.literal("Minecraft username"));field.setMaxLength(16);field.setPlaceholder(Text.literal("Minecraft username"));field.setText(name);field.setChangedListener(v->name=v);addDrawableChild(field);
        boolean owner=CommunityService.owner(client.getSession().getUuidOrNull()==null?"":client.getSession().getUuidOrNull().toString());
        var ranks=BadgeRank.assignable(owner);statusY=90+((ranks.size()+1)/2)*27;
        if(status.isEmpty())status=owner?"Your Owner badge is orange and reserved for Tarrecool.":"Co-Owners can grant Partner or Mod. Admin, Co-Owner and Owner badges are protected.";
        for(int i=0;i<ranks.size();i++){var rank=ranks.get(i);var button=new TarButton(x+(i%2)*(w/2+3),82+(i/2)*27,w/2-3,22,rank.label,b->{if(!name.matches("[A-Za-z0-9_]{1,16}")){status="Enter a valid Minecraft username.";return;}busy=true;status="Updating rank...";clearAndInit();CommunityBadges.manage(name,rank.id,message->{busy=false;status=message;clearAndInit();});});button.active=!busy&&CommunityBadges.canManageRanks();addDrawableChild(button);}
        addDrawableChild(new TarButton(width/2-45,height-28,90,20,"Done",b->close()));
    }
    public void render(DrawContext c,int x,int y,float delta){c.fill(0,0,width,height,UiTheme.color(0xF0101720));c.drawCenteredTextWithShadow(textRenderer,"MANAGE TAR RANKS",width/2,22,UiTheme.color(0xFFE8EDF5));int lineY=statusY;for(var line:textRenderer.wrapLines(Text.literal(status),width-24)){if(lineY>height-40)break;c.drawTextWithShadow(textRenderer,line,12,lineY,UiTheme.color(0xFFE8EDF5));lineY+=10;}super.render(c,x,y,delta);}
    public void close(){client.setScreen(parent);}public boolean shouldPause(){return false;}
}
