package dev.tarclient.mod;

import net.fabricmc.loader.api.FabricLoader;
import static dev.tarclient.mod.TarClient.CONFIG;

/** Optional upstream API: no Minecraft names are resolved through reflection. */
public final class MotionBlurBridge {
    private record State(boolean enabled,int strength,boolean pause) {}
    private static State last;
    private static boolean failed;
    private static java.lang.reflect.Field configField,enabledField,strengthField,pauseField;
    private static java.lang.reflect.Method saveMethod,resetMethod;
    private static State desired(){return new State(CONFIG.on("motionblur"),CONFIG.i("motionblur","strength"),CONFIG.bool("motionblur","pauseInGuis"));}
    public static String status(){
        if(failed)return "Settings unavailable: check the Minecraft log.";
        return FabricLoader.getInstance().isModLoaded("motionblur")?"Installed: changes apply immediately.":"Enable, then relaunch Minecraft to install.";
    }
    public static void tick(){
        if(failed||!FabricLoader.getInstance().isModLoaded("motionblur"))return;
        try{
            if(configField==null){
                var cls=Class.forName("com.jahleel.motionblur.client.MotionBlurClient");
                configField=cls.getField("config");var type=configField.getType();
                enabledField=type.getField("enabled");strengthField=type.getField("strength");pauseField=type.getField("pauseInGuis");saveMethod=type.getMethod("save");
                resetMethod=Class.forName("com.jahleel.motionblur.client.MotionBlurRenderer").getMethod("reset");
            }
            Object upstream=configField.get(null);if(upstream==null)return;
            State tar=desired();
            if(!tar.equals(last)){
                enabledField.setBoolean(upstream,tar.enabled());strengthField.setFloat(upstream,tar.strength()/20f);pauseField.setBoolean(upstream,tar.pause());
                saveMethod.invoke(upstream);if(!tar.enabled())resetMethod.invoke(null);last=tar;
            }else{
                // Keep upstream commands, shortcuts and Mod Menu changes in saved Tar profiles.
                State live=new State(enabledField.getBoolean(upstream),Math.clamp(Math.round(strengthField.getFloat(upstream)*20),1,100),pauseField.getBoolean(upstream));
                if(!live.equals(last)){
                    CONFIG.set("motionblur","enabled",live.enabled());CONFIG.set("motionblur","strength",live.strength());CONFIG.set("motionblur","pauseInGuis",live.pause());last=live;TarClient.save();
                }
            }
        }catch(ReflectiveOperationException|LinkageError e){failed=true;org.slf4j.LoggerFactory.getLogger("TarClient").error("Cannot configure Smooth Motion Blur; its API may have changed",e);}
    }
}
