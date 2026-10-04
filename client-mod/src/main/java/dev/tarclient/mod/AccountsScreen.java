package dev.tarclient.mod;

import dev.tarclient.launcher.MicrosoftAuth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.net.URI;
import java.util.*;
import java.util.concurrent.FutureTask;

/** Only verified Microsoft sessions; passwords and tokens are never written to a Tar file. */
public final class AccountsScreen extends Screen {
    private static final Map<String,MicrosoftAuth.Session> accounts=new LinkedHashMap<>();
    private final Screen parent;
    private String status="Accounts stay in memory until Minecraft closes.";
    private MicrosoftAuth.DeviceCode code;
    private FutureTask<Void> task;
    private boolean busy;
    private int generation,page;
    public AccountsScreen(Screen parent){super(Text.literal("Tar accounts"));this.parent=parent;}
    @Override protected void init(){
        var current=client.getSession();
        if(!client.isDemo()&&!current.getAccessToken().equals("0")&&current.getUuidOrNull()!=null){
            String id=current.getUuidOrNull().toString().replace("-","");
            accounts.putIfAbsent(id,new MicrosoftAuth.Session(current.getUsername(),id,current.getAccessToken(),Long.MAX_VALUE,false));
        }
        int w=Math.min(460,width-24),x=(width-w)/2;
        var add=new TarButton(x,52,w,22,busy?"Waiting for Microsoft...":"Add Microsoft / Minecraft account",b->start(null));add.active=!busy;addDrawableChild(add);
        if(code!=null&&busy){
            addDrawableChild(new TarButton(x,105,w/2-3,22,"Open Microsoft",b->Util.getOperatingSystem().open(URI.create(code.url()))));
            addDrawableChild(new TarButton(x+w/2+3,105,w/2-3,22,"Copy code",b->client.keyboard.setClipboard(code.code())));
        }else{
            var list=new ArrayList<>(accounts.values());int count=Math.max(1,(height-176)/28);page=Math.clamp(page,0,Math.max(0,(list.size()-1)/count));
            for(int i=page*count;i<Math.min(list.size(),(page+1)*count);i++){
                var account=list.get(i);int y=86+(i-page*count)*28;
                boolean active=current.getUuidOrNull()!=null&&current.getUuidOrNull().toString().replace("-","").equals(account.uuid().replace("-",""));
                var use=new TarButton(x,y,w-80,22,(active?"Current: ":"Switch to: ")+account.name(),b->start(account));use.active=!busy&&!active;addDrawableChild(use);
                var forget=new TarButton(x+w-74,y,74,22,"Forget",b->{accounts.remove(account.uuid());clearAndInit();});forget.active=!busy&&!active;addDrawableChild(forget);
            }
            if(page>0)addDrawableChild(new TarButton(x,height-85,40,20,"<",b->{page--;clearAndInit();}));
            if((page+1)*count<list.size())addDrawableChild(new TarButton(x+w-40,height-85,40,20,">",b->{page++;clearAndInit();}));
        }
        addDrawableChild(new TarButton(width/2-60,height-28,120,20,busy?"Cancel sign-in":"Done",b->{if(busy){cancel();status="Sign-in cancelled. Your account is unchanged.";clearAndInit();}else close();}));
    }
    private void start(MicrosoftAuth.Session saved){
        if(busy)return;busy=true;code=null;status="Verifying Microsoft and Minecraft access...";int request=++generation;clearAndInit();
        MinecraftClient mc=client;
        task=new FutureTask<>(()->{
            try{
                var auth=saved!=null?saved:new MicrosoftAuth().login(MicrosoftAuth.DEFAULT_CLIENT_ID,device->mc.execute(()->{
                    if(generation!=request)return;code=device;status="Enter the code on Microsoft's page. Passwords stay with Microsoft.";clearAndInit();
                }));
                var prepared=PreparedAccount.prepare(auth,mc);
                mc.execute(()->{if(generation!=request)return;busy=false;code=null;task=null;accounts.put(auth.uuid().replace("-",""),auth);confirmSwitch(prepared);});
            }catch(Exception failure){mc.execute(()->{if(generation!=request)return;busy=false;code=null;task=null;status="Sign-in failed. Check Minecraft ownership, connection and account permissions, then try again.";clearAndInit();});}
            return null;
        });
        Thread.ofVirtual().name("Tar Microsoft sign-in").start(task);
    }
    private void confirmSwitch(PreparedAccount next){
        if(client.world==null){apply(next);return;}
        client.setScreen(new ConfirmScreen(yes->{
            client.setScreen(this);
            if(yes)client.getAbuseReportContext().tryShowDraftScreen(client,this,()->apply(next),true);
            else{status="Account added. Your current game is unchanged.";clearAndInit();}
        },Text.literal("Switch to "+next.session().getUsername()+"?"),Text.literal("This leaves your world or server. Minecraft stays open; reconnect after switching."),Text.literal("Leave and switch"),Text.literal("Stay here")));
    }
    private void apply(PreparedAccount next){
        try{
            if(client.world!=null)client.disconnectWithSavingScreen();
            ((AccountSessionAccess)client).tar$applyAccount(next);
            client.setScreen(new AccountsScreen(new TitleScreen()));
        }catch(Exception e){var screen=new AccountsScreen(new TitleScreen());screen.status="Account switch failed. Selected account: "+client.getSession().getUsername()+". Try signing in again.";client.setScreen(screen);}
    }
    private void cancel(){generation++;busy=false;code=null;if(task!=null){task.cancel(true);task=null;}}
    @Override public void removed(){if(busy)cancel();}
    @Override public void close(){cancel();client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
    @Override public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,0xF0101720);c.drawCenteredTextWithShadow(textRenderer,"YOUR ACCOUNTS",width/2,16,0xFFB9F47A);
        c.drawCenteredTextWithShadow(textRenderer,"Playing as "+client.getSession().getUsername(),width/2,32,0xFFE8EDF5);
        if(code!=null&&busy)c.drawCenteredTextWithShadow(textRenderer,code.code(),width/2,86,0xFFB9F47A);
        int y=height-61;for(var line:textRenderer.wrapLines(Text.literal(status),width-28)){c.drawTextWithShadow(textRenderer,line,14,y,0xFF9DADBF);y+=10;}
        super.render(c,mx,my,delta);
    }
}
