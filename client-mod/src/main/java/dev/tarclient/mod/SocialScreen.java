package dev.tarclient.mod;
import com.google.gson.*;
import dev.tarclient.config.*;
import dev.tarclient.launcher.SocialClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import java.util.*;
/** Network work is serialized off the render thread; typed drafts survive refreshes. */
public final class SocialScreen extends Screen {
    private final Screen parent;
    private SocialState state=SocialState.EMPTY;
    private SocialState.Person selected;
    private java.util.List<SocialState.Message> messages=java.util.List.of();
    private String tab="Friends",draft="",name="",status="Connecting...";
    private int page,messagePage;
    private long next;
    private boolean busy;
    public SocialScreen(Screen parent){super(Text.literal("Tar friends & style"));this.parent=parent;}
    private void call(String action,JsonObject body,java.util.function.Consumer<JsonObject> then){if(busy)return;busy=true;SocialRuntime.request(action,body,r->{busy=false;then.accept(r);},e->{busy=false;status=e;});}
    private void refresh(){call("state",new JsonObject(),r->{var fresh=SocialState.parse(r);boolean changed=!fresh.equals(state);state=fresh;if(selected!=null){selected=state.people().stream().filter(p->p.uuid().equals(selected.uuid())).findFirst().orElse(null);if(selected==null)messages=java.util.List.of();}status="All emotes and cosmetics are free.";if(changed)clearAndInit();fetchMessages();});}
    private void fetchMessages(){if(selected!=null&&selected.kind().equals("friend")){String id=selected.uuid();call("messages",SocialClient.body("target",id),r->{if(selected!=null&&selected.uuid().equals(id))messages=SocialState.messages(r);});}}
    protected void init(){
        int w=Math.min(width-24,650),x=(width-w)/2;
        String[] tabs={"Friends","Cosmetics","Emotes"};for(int i=0;i<tabs.length;i++){String name=tabs[i];addDrawableChild(new TarButton(x+i*w/3,32,w/3-4,20,name,b->{tab=name;page=0;clearAndInit();},()->tab.equals(name)));}
        if(tab.equals("Friends"))friends(x,w);else wardrobe(x,w);
        addDrawableChild(new TarButton(width/2-45,height-27,90,20,"Done",b->close()));
    }
    private void friends(int x,int w){
        if(selected!=null){
            var who=selected;addDrawableChild(new TarButton(x,62,70,20,"< People",b->{selected=null;messages=java.util.List.of();messagePage=0;clearAndInit();}));
            if(who.kind().equals("incoming"))addDrawableChild(new TarButton(x+76,62,80,20,"Accept",b->respond("accept")));
            addDrawableChild(new TarButton(x+w-150,62,70,20,who.kind().equals("blocked")?"Unblock":"Block",b->respond(who.kind().equals("blocked")?"unblock":"block")));
            if(!who.kind().equals("blocked"))addDrawableChild(new TarButton(x+w-76,62,76,20,who.kind().equals("friend")?"Remove":"Decline",b->respond("remove")));
            if(who.kind().equals("friend")){
                var field=field(x,height-82,w-66,draft,1000,"Message "+who.name(),v->draft=v);
                addDrawableChild(new TarButton(x+w-60,height-82,60,22,"Send",b->send()));
                addDrawableChild(new TarButton(x,height-110,70,20,"Older",b->messagePage++));
                addDrawableChild(new TarButton(x+w-70,height-110,70,20,"Newer",b->messagePage=Math.max(0,messagePage-1)));
            }
            return;
        }
        field(x,62,w-110,name,16,"Minecraft username",v->name=v);
        addDrawableChild(new TarButton(x+w-104,62,104,22,"Send request",b->call("request",SocialClient.body("name",name.trim()),r->{name="";status="Friend request sent";next=0;clearAndInit();})));
        int count=Math.max(1,(height-160)/26);page=Math.clamp(page,0,Math.max(0,(state.people().size()-1)/count));
        for(int i=page*count;i<Math.min(state.people().size(),(page+1)*count);i++){var p=state.people().get(i);addDrawableChild(new TarButton(x,96+(i-page*count)*26,w,22,p.toString(),b->{selected=p;messages=java.util.List.of();messagePage=0;clearAndInit();fetchMessages();}));}
        pages(x,w,count,state.people().size());
    }
    private TextFieldWidget field(int x,int y,int w,String value,int limit,String hint,java.util.function.Consumer<String> change){var f=new TextFieldWidget(textRenderer,x,y,w,22,Text.literal(hint));f.setMaxLength(limit);f.setText(value);f.setPlaceholder(Text.literal(hint));f.setEditableColor(0xFFFFFFFF);f.setChangedListener(change);return addDrawableChild(f);}
    private void respond(String action){var who=selected;if(who==null)return;Runnable apply=()->call("respond",SocialClient.body("target",who.uuid(),"action",action),r->{selected=null;messages=java.util.List.of();status="Updated";next=0;clearAndInit();});if(action.equals("block")||action.equals("remove"))client.setScreen(new ConfirmScreen(yes->{client.setScreen(this);if(yes)apply.run();},Text.literal(action.equals("block")?"Block this player?":"Remove this connection?"),Text.literal("This also deletes your shared message history.")));else apply.run();}
    private void send(){if(selected==null||draft.isBlank())return;String id=selected.uuid(),sent=draft;call("send",SocialClient.body("target",id,"text",sent,"nonce",UUID.randomUUID().toString()),r->{if(selected!=null&&selected.uuid().equals(id)){messages=SocialState.messages(r);if(draft.equals(sent))draft="";messagePage=0;clearAndInit();}});}
    private void wardrobe(int x,int w){
        boolean cosmetics=tab.equals("Cosmetics");var items=cosmetics?SocialCatalog.COSMETICS:SocialCatalog.EMOTES;
        int count=Math.max(1,(height-174)/26)*2;page=Math.clamp(page,0,Math.max(0,(items.size()-1)/count));
        for(int i=page*count;i<Math.min(items.size(),(page+1)*count);i++){var item=items.get(i);int n=i-page*count;boolean equipped=state.cosmetics().contains(item.id());addDrawableChild(new TarButton(x+(n%2)*(w/2),88+(n/2)*26,w/2-4,22,(equipped?"✓ ":"")+item.name(),b->{if(cosmetics)call("equip",SocialState.equipment(SocialCatalog.equip(state.cosmetics(),item.id())),r->{next=0;status="Outfit saved";});else SocialRuntime.emote(item.id(),s->status=s);},()->cosmetics&&state.cosmetics().contains(item.id())));}
        if(!cosmetics)addDrawableChild(new TarButton(x+w-100,60,100,20,"Stop emote",b->SocialRuntime.emote("",s->status=s)));
        pages(x,w,count,items.size());
    }
    private void pages(int x,int w,int count,int total){if(page>0)addDrawableChild(new TarButton(x,height-54,40,20,"<",b->{page--;clearAndInit();}));if((page+1)*count<total)addDrawableChild(new TarButton(x+w-40,height-54,40,20,">",b->{page++;clearAndInit();}));}
    public void tick(){if(!busy&&System.currentTimeMillis()>next){next=System.currentTimeMillis()+6000;refresh();}}
    public boolean keyPressed(KeyInput input){if(input.key()==257&&tab.equals("Friends")&&selected!=null&&selected.kind().equals("friend")){send();return true;}return super.keyPressed(input);}
    public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,UiTheme.color(0xFF101720));int w=Math.min(width-24,650),x=(width-w)/2;
        c.drawCenteredTextWithShadow(textRenderer,"FRIENDS & STYLE",width/2,14,UiTheme.color(0xFFA5F078));
        if(!tab.equals("Friends"))c.drawTextWithShadow(textRenderer,SocialRuntime.sharing()?"Free for everyone · visible to other Tar players":"Free · sharing is off in launcher Settings",x,66,0xFFCCCCCC);
        if(selected!=null&&tab.equals("Friends")){
            c.drawTextWithShadow(textRenderer,selected.name()+" · "+selected.kind(),x,91,0xFFFFFFFF);
            var lines=new ArrayList<net.minecraft.text.OrderedText>();String me=SocialRuntime.session().uuid();
            for(var message:messages)lines.addAll(textRenderer.wrapLines(Text.literal((message.sender().equals(me)?"You":selected.name())+": "+message.text()),w-10));
            int rows=Math.max(1,(height-224)/11);messagePage=Math.min(messagePage,Math.max(0,(lines.size()-1)/rows));int end=Math.max(0,lines.size()-messagePage*rows),start=Math.max(0,end-rows);int y=110;
            c.enableScissor(x,106,x+w,height-114);for(int i=start;i<end;i++){c.drawTextWithShadow(textRenderer,lines.get(i),x+4,y,0xFFE8EDF5);y+=11;}c.disableScissor();
        }
        c.drawCenteredTextWithShadow(textRenderer,textRenderer.trimToWidth((busy?"Connecting… ":"")+status,w-90),width/2,height-46,0xFFCCCCCC);super.render(c,mx,my,delta);
    }
    public boolean shouldPause(){return false;}
    public void close(){client.setScreen(parent);}
}
