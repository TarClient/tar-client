package dev.tarclient.mod;
import dev.tarclient.config.ProfileStore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.List;
public final class ProfilesScreen extends Screen {
    private final Screen parent;
    private final ProfileStore store=new ProfileStore(FabricLoader.getInstance().getConfigDir());
    private String status="Create a named copy of your current module settings.",draft="";
    private int page;
    public ProfilesScreen(Screen parent){super(Text.literal("Tar profiles"));this.parent=parent;}
    protected void init(){
        int w=Math.min(420,width-20),x=(width-w)/2,createWidth=112;
        var name=new TextFieldWidget(textRenderer,x,48,w-createWidth-6,22,Text.literal("New profile name"));
        name.setMaxLength(48);name.setText(draft);name.setPlaceholder(Text.literal("New profile name"));name.setChangedListener(v->draft=v);addDrawableChild(name);
        addDrawableChild(new TarButton(x+w-createWidth,48,createWidth,22,"Create profile",b->{try{
            String value=draft.trim();store.create(value,TarClient.CONFIG);status="Created "+value+" from your current setup.";draft="";
            int count=Math.max(1,(height-156)/28);page=store.list().indexOf(value)/count;clearAndInit();
        }catch(Exception e){status=e.getMessage();}}));
        try{
            store.presets();List<String> profiles=store.list();int count=Math.max(1,(height-156)/28);page=Math.clamp(page,0,Math.max(0,(profiles.size()-1)/count));
            for(int i=page*count;i<Math.min(profiles.size(),(page+1)*count);i++){
                String profile=profiles.get(i);int y=82+(i-page*count)*28;
                addDrawableChild(new TarButton(x,y,w-146,22,"Load  "+profile,b->{try{TarClient.CONFIG=store.load(profile);TarClient.save();status="Loaded "+profile+". Other integrations apply next launch.";}catch(Exception e){status=e.getMessage();}}));
                addDrawableChild(new TarButton(x+w-140,y,68,22,"Update",b->client.setScreen(new ConfirmScreen(yes->{client.setScreen(this);if(yes)try{store.save(profile,TarClient.CONFIG);status="Updated "+profile+" with your current setup.";}catch(Exception e){status=e.getMessage();}},Text.literal("Update profile?"),Text.literal("Replace settings saved in "+profile+"?")))));
                addDrawableChild(new TarButton(x+w-66,y,66,22,"Delete",b->client.setScreen(new ConfirmScreen(yes->{client.setScreen(this);if(yes)try{store.delete(profile);status="Deleted "+profile+". A backup is in tar-profiles/deleted.";clearAndInit();}catch(Exception e){status=e.getMessage();}},Text.literal("Delete profile?"),Text.literal("Delete "+profile+"? Your current modules stay as they are.")))));
            }
            if(page>0)addDrawableChild(new TarButton(x,height-56,45,20,"<",b->{page--;clearAndInit();}));
            if((page+1)*count<profiles.size())addDrawableChild(new TarButton(x+w-45,height-56,45,20,">",b->{page++;clearAndInit();}));
        }catch(Exception e){status=e.getMessage();}
        addDrawableChild(new TarButton(width/2-45,height-30,90,20,"Done",b->close()));
    }
    public void render(DrawContext c,int x,int y,float delta){c.fill(0,0,width,height,UiTheme.color(0xEF101720));c.drawCenteredTextWithShadow(textRenderer,"YOUR PROFILES",width/2,22,UiTheme.color(0xFFA5F078));c.drawCenteredTextWithShadow(textRenderer,textRenderer.trimToWidth(status,width-20),width/2,height-76,UiTheme.color(0xFFE8EDF5));super.render(c,x,y,delta);}
    public void close(){client.setScreen(parent);}
    public boolean shouldPause(){return false;}
}
