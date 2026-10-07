package dev.tarclient.mod;

import dev.tarclient.config.AppleSkinSettings;
import net.fabricmc.loader.api.FabricLoader;

public final class AppleSkinBridge {
    private static java.lang.reflect.Field instanceField;
    private static Object instance;
    private static AppleSkinSettings.Binding binding;
    private static boolean failed;
    public static String status(){
        if(failed)return "AppleSkin settings unavailable; check game log.";
        return FabricLoader.getInstance().isModLoaded("appleskin")
            ?"AppleSkin hunger bar / changes apply live."
            :"Relaunch from Tar to install AppleSkin.";
    }
    public static void tick(){
        if(failed||!FabricLoader.getInstance().isModLoaded("appleskin"))return;
        try{
            if(instanceField==null)instanceField=Class.forName("squeek.appleskin.ModConfig").getField("INSTANCE");
            Object current=instanceField.get(null);if(current==null)return;
            if(current!=instance){binding=new AppleSkinSettings.Binding(current);instance=current;}
            // Only change in-memory display options. Keep the user's upstream
            // config file intact; Tar's saved profile is the source of these options.
            binding.apply(TarClient.CONFIG);
        }catch(ReflectiveOperationException|LinkageError e){
            failed=true;org.slf4j.LoggerFactory.getLogger("TarClient").error("Cannot configure AppleSkin; its public config API may have changed",e);
        }
    }
}
