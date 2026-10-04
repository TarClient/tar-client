package dev.tarclient.mod;

import java.util.*;
import dev.tarclient.config.ClientConfig;
import dev.tarclient.config.Integrations;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import static dev.tarclient.mod.TarClient.CONFIG;

public final class TarKeybinds {
    private static final Map<String,KeyBinding> toggles=new LinkedHashMap<>();
    private static KeyBinding profiles,time,accounts,reroll;
    public static void initialize(){
        var category=KeyBinding.Category.create(Identifier.of("tarclient","toggles"));
        for(var module:ClientConfig.MODULES)if(!module.id().equals("profiles"))
            toggles.put(module.id(),register("toggle."+module.id(),category));
        profiles=register("profiles",category);time=register("time",category);accounts=register("accounts",category);reroll=register("streamer.reroll",category);
    }
    private static KeyBinding register(String id,KeyBinding.Category category){return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.tarclient."+id,InputUtil.Type.KEYSYM,-1,category));}
    public static void tick(MinecraftClient client){
        boolean allowed=client.player!=null&&client.currentScreen==null&&client.isWindowFocused();
        for(var entry:toggles.entrySet())while(entry.getValue().wasPressed())if(allowed){
            String id=entry.getKey();CONFIG.set(id,"enabled",!CONFIG.on(id));TarClient.save();
            String name=ClientConfig.MODULES.stream().filter(m->m.id().equals(id)).findFirst().orElseThrow().name();
            String suffix=Integrations.find(id)!=null&&!id.equals("motionblur")?" (next launch)":"";
            if(id.equals("motionblur")&&!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("motionblur"))suffix=" (next launch)";
            client.player.sendMessage(Text.literal(name+": "+(CONFIG.on(id)?"ON":"OFF")+suffix),true);
        }
        while(profiles.wasPressed())if(allowed){client.setScreen(new ProfilesScreen(null));allowed=false;}
        while(accounts.wasPressed())if(allowed){client.setScreen(new AccountsScreen(null));allowed=false;}
        while(reroll.wasPressed())if(allowed&&StreamerMode.active())StreamerMode.reroll();
        while(time.wasPressed())if(allowed)ClientFeatures.applyTime();
        // Keyboard shortcuts are intercepted to allow the menu over other screens;
        // mouse bindings are handled by MouseMixin. Drain vanilla's queued presses.
        while(TarClient.MENU_KEY.wasPressed()){}
    }
}
