package dev.tarclient.config;

import com.google.gson.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Shared schema: the launcher and the in-game editor use exactly the same settings. */
public final class ClientConfig {
    public record Setting(String key, String label, Object initial, double min, double max, double step) {}
    public record Module(String id, String name, String category, String description, List<Setting> settings) {}
    public static final List<Module> MODULES = new ArrayList<>();
    static Setting b(String k,String l,boolean v) { return new Setting(k,l,v,0,1,1); }
    static Setting n(String k,String l,double v,double min,double max,double step) { return new Setting(k,l,v,min,max,step); }
    static Setting s(String k,String l,String v) { return new Setting(k,l,v,0,0,0); }
    static void add(String id,String name,String cat,String desc,boolean on,Setting... settings) {
        var list = new ArrayList<Setting>(); list.add(b("enabled","Enabled",on)); list.addAll(List.of(settings));
        MODULES.add(new Module(id,name,cat,desc,List.copyOf(list)));
    }
    static Setting[] hud(int x,int y,Setting... extra) {
        var list=new ArrayList<Setting>(List.of(n("x","Horizontal position (%)",x,0,100,1),n("y","Vertical position (%)",y,0,100,1),n("scale","HUD scale",1,0.5,3,0.1),b("showBackground","Show background",false),s("backgroundStyle","Background style","rounded"),s("backgroundEnd","Gradient end (ARGB hex)","B83C4A66"),n("radius","Background roundness",5,0,20,1),s("color","Text color (hex)","E8EDF5"),s("background","Background (ARGB hex)","B8181D29")));
        list.addAll(List.of(extra)); return list.toArray(Setting[]::new);
    }
    static {
        add("fps","FPS","HUD","Live frames per second.",true,hud(2,2));
        add("ping","Ping","HUD","Your server latency; singleplayer is shown separately.",true,hud(2,7));
        add("armor","Armor status","HUD","Durability for every armor slot, with a repeating low-durability alert.",true,hud(2,72,n("threshold","Alert below (%)",15,1,50,1),b("sound","Play warning sound",true),n("volume","Warning volume",0.7,0,1,0.1),n("cooldown","Warning interval (seconds)",10,2,120,1),b("percent","Show percent",true),b("horizontal","Horizontal layout",true),b("bar","Durability bars",true),b("empty","Show empty slots",false),b("reverse","Reverse direction (up / left)",false),b("text","Show durability text",true),n("iconSize","Armor icon size",16,12,32,1),n("spacing","Space between slots",6,0,24,1),n("barWidth","Durability bar width",30,12,80,1),b("durabilityColors","Color by durability",true)));
        add("potions","Potion status","HUD","Timers and levels on Minecraft effect icons in the top-right corner.",true,b("beneficial","Show beneficial effects",true),b("harmful","Show harmful effects",true),b("duration","Show remaining time",true),b("amplifier","Show effect level",true),b("showBackground","Show vanilla icon background",true),n("scale","Icon scale",1,0.5,2,0.1),n("margin","Corner padding",2,0,40,1),s("color","Timer color (hex)","FFFFFF"),s("expiringColor","Expiring timer color (hex)","FF7878"),s("backgroundStyle","Background style","vanilla"),s("background","Background (ARGB hex)","B8181D29"),s("backgroundEnd","Gradient end (ARGB hex)","B83C4A66"),n("radius","Background roundness",5,0,20,1));
        add("keys","Keystrokes + mouse","HUD","Movement keys, jump, mouse buttons and left/right CPS.",true,hud(2,18,b("mouse","Show mouse buttons",true),b("cps","Show clicks per second",true),s("pressed","Pressed color (hex)","A5F078")));
        add("crosshair","Custom crosshair","Visual","Keep the vanilla crosshair, tune a simple cross, or draw your own pixel design.",true,b("vanilla","Use original Minecraft crosshair",true),b("grid","Use grid design (turn vanilla off)",false),s("pattern","Pixel design",CrosshairPattern.vanilla()),n("pixelScale","Grid pixel scale",1,1,4,1),n("size","Arm length",5,1,30,1),n("gap","Center gap",0,0,20,1),n("thickness","Thickness",1,1,8,1),s("color","Color (hex)","FFFFFF"),b("dot","Center dot",true),b("outline","Black outline",false),b("thirdPerson","Show in third person",false));
        add("fullbright","Fullbright","Visual","Light every lightmap level, including unlit caves; does not change server light or mob spawning.",false,n("strength","Brightness",1,0,1,0.05));
        add("nofog","No fog","Visual","Remove terrain fog; fluid fog can be changed separately.",false,b("fluids","Also remove water/lava fog",false));
        add("items","Item size","Visual","Scale all rendered items by context. Per-item overrides use namespace:item=scale.",false,n("hand","First-person scale",0.8,0.1,2.5,0.05),n("gui","Inventory scale",1,0.1,2,0.05),n("ground","Dropped item scale",1,0.1,3,0.05),n("thirdPerson","Third-person scale",1,0.1,3,0.05),n("fixed","Item frame / display scale",1,0.1,3,0.05),s("overrides","Per-item overrides (semicolon separated)",""),s("filterMode","Apply to items","all"),s("filterItems","Item IDs (comma separated)","minecraft:shield"));
        add("shield","Low / side shield","Visual","Adjust the held shield position while preserving blocking behavior.",true,n("down","Lower shield",0.3,0,1.5,0.05),n("side","Move shield outward",0.15,0,1.5,0.05));
        add("fire","Low fire","Visual","Lower the on-screen fire overlay.",true,n("offset","Lower fire",0.5,0,1.5,0.05));
        add("glint","Enchantment glint","Visual","Disable glint or tune the vanilla glint strength and speed.",true,b("hidden","Hide glint",false),n("strength","Glint strength",0.4,0,1,0.05),n("speed","Glint speed",0.5,0,1,0.05));
        add("hitboxes","Hitbox outlines","Visual","Customize F3+B debug outlines. Collision and reach stay vanilla.",false,b("always","Show without F3+B",false),s("color","Outline color (hex)","A5F078"),b("eyeLine","Show eye-height box",true),b("direction","Show view direction",true));
        add("borderless","Borderless fullscreen","Window","F11 uses a borderless monitor-sized window.",true);
        add("clock","Clock","HUD","Your computer's local time in AM/PM format.",false,hud(82,2,b("seconds","Show seconds",false)));
        add("coordinates","Coordinates","HUD","Your block coordinates and dimension.",false,hud(2,12,b("dimension","Show dimension",true)));
        add("inventory","Inventory HUD","HUD","Your inventory without opening it.",false,hud(50,75,b("hotbar","Include hotbar",true),b("counts","Show item counts and durability",true)));
        add("saturation","Saturation","HUD","Exact in singleplayer. Multiplayer only exposes the client's estimate unless the server syncs it.",false,hud(45,90));
        add("reach","Reach display","HUD","Distance to the target's hitbox at your last attack; a local measurement, not server-confirmed damage.",false,hud(45,5,n("seconds","Display duration (seconds)",4,1,30,1),b("playersOnly","Only measure players",true)));
        add("server","Server address","HUD","Current server address with its server-list icon.",false,hud(65,92,b("icon","Show server icon",true),b("name","Show server name",true)));
        add("zoom","Zoom","Visual","Hold C to zoom; rebind in Minecraft Controls.",false,n("factor","Zoom multiplier",4,1.5,15,0.5),b("smooth","Smooth transition",true),n("scrollStep","Scroll zoom step",0.5,0.1,3,0.1));
        add("freelook","Freelook","Visual","Hold Left Alt to look around in third person without turning your player. Rebind in Controls.",false,n("sensitivity","Camera sensitivity",1,0.1,3,0.1));
        add("shulker","Shulker box tooltips","Visual","Preview the contents of shulker boxes in a 9-column item grid.",true,b("shift","Require Shift",false),b("showBackground","Show background",true),s("background","Background (ARGB hex)","ED151C29"),s("backgroundStyle","Background style","rounded"),s("backgroundEnd","Gradient end (ARGB hex)","ED3C4A66"),n("radius","Background roundness",5,0,20,1));
        add("hitcolor","Hit color","Visual","Tint players when they take damage.",false,s("color","Damage color (hex)","FF6868"));
        add("timechanger","Time changer","Utility","Applies /time set only when you choose Apply. Requires server permission or singleplayer cheats.",false,n("time","Time of day (ticks)",1000,0,23999,100));
        add("streamer","Streamer mode","Utility","Random display coordinates in Tar HUD, vanilla F3 and BetterF3. Does not hide coordinates in chat, maps or other mods.",false);
        add("badges","Tar player badges","Utility","Show the Tar logo for players with a recent verified Tar session. Shares your Minecraft UUID with Tar community while playing. Only Tarrecool grants ranks.",true);
        add("darkmode","Dark mode","Utility","Dark or light appearance for Tar menus. Launcher theme is in Settings.",true);
        add("tps","TPS","HUD","Estimated server ticks per second from time updates. Network delay can affect the estimate.",false,hud(2,42,n("samples","Smoothing samples",5,1,20,1)));
        add("particles","Particles","Visual","Control newly spawned particles. 100% is vanilla; zero hides the selected type. Does not create extra hits.",false,n("all","All particles (%)",100,0,200,5),n("crit","Critical particles (%)",100,0,200,5),n("potion","Potion particles (%)",100,0,200,5),n("other","Other particles (%)",100,0,200,5));
        add("tnttimer","TNT timer","HUD","Remaining fuse for visible primed TNT under your crosshair, using synchronized game ticks.",false,hud(48,57,n("range","Maximum distance",32,4,64,1)));
        add("mousetracer","Mouse tracer","HUD","A trail of your recent mouse movement inside a small HUD panel.",false,hud(78,65,n("width","Panel width",100,50,240,10),n("height","Panel height",70,40,160,10),n("seconds","Trail duration",0.6,0.1,3,0.1),n("sensitivity","Movement scale",0.4,0.05,2,0.05)));
        add("itemcounter","Item counter","HUD","Counts selected items in your inventory and offhand. Enter item IDs separated by commas.",false,hud(82,30,s("items","Items to count","minecraft:mushroom_stew,minecraft:golden_apple,minecraft:potion,minecraft:arrow"),b("hideZero","Hide empty counts",false)));
        add("text","Text","HUD","Your own on-screen text. Use \\n in the text field for a new line.",false,hud(40,20,s("text","Your text","Tar Client")));
        add("profiles","Profiles","Utility","Save and load complete module settings. Includes Bedwars and SMP starting profiles.",true);
        add("disconnect","Smart disconnect","Utility","Confirm before leaving through the pause menu.",true);
        add("unfocused","Limit unfocused FPS","Window","Cap FPS while Minecraft is not the focused window.",true,n("fps","Unfocused FPS limit",30,5,120,5));
        add("tiertagger","TierTagger","Integrations","Official MCTiers TierTagger. Downloaded from Modrinth; restart Minecraft after changing enabled state. Settings available in game.",false);
        add("motionblur","Motion blur","Integrations","Smooth Motion Blur. First installation needs a relaunch; strength, menu pause and toggles then apply live.",false,n("strength","Blur strength (1-100)",20,1,100,1),b("pauseInGuis","Pause blur in menus",true));
        add("packorganizer","Pack organizer","Integrations","Resource Tree adds folders, creation and navigation in the Resource Packs screen. Restart after changing enabled state.",false);
    }
    private final JsonObject values;
    public ClientConfig() { this(new JsonObject()); }
    private ClientConfig(JsonObject values) { this.values=values; validate(); }
    public static ClientConfig read(Path path) throws IOException {
        if (!Files.exists(path)) return new ClientConfig();
        try (var reader=Files.newBufferedReader(path)) { return new ClientConfig(JsonParser.parseReader(reader).getAsJsonObject()); }
        catch (RuntimeException e) { throw new IOException("Invalid settings in "+path+". Rename it to reset settings.",e); }
    }
    public synchronized void save(Path path) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path tmp=Files.createTempFile(path.toAbsolutePath().getParent(),"tar-settings-",".tmp");
        try { Files.writeString(tmp,new GsonBuilder().setPrettyPrinting().create().toJson(values));
            try { Files.move(tmp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException e) { Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(tmp); }
    }
    public synchronized JsonElement get(String module,String key) { return values.getAsJsonObject(module).get(key); }
    public boolean on(String module) { return bool(module,"enabled"); }
    public boolean bool(String m,String k) { return get(m,k).getAsBoolean(); }
    public double number(String m,String k) { return get(m,k).getAsDouble(); }
    public float f(String m,String k) { return (float)number(m,k); }
    public int i(String m,String k) { return (int)Math.round(number(m,k)); }
    public String text(String m,String k) { return get(m,k).getAsString(); }
    public int color(String m,String k) {
        try { String v=text(m,k).replace("#",""); long c=Long.parseLong(v,16); return (int)(v.length()<=6?c|0xFF000000L:c); }
        catch(Exception e) { return 0xFFE8EDF5; }
    }
    public synchronized void set(String m,String k,Object value) {
        if(m.equals("crosshair")&&k.equals("pattern"))value=CrosshairPattern.normalize(String.valueOf(value));
        values.getAsJsonObject(m).add(k,new Gson().toJsonTree(value)); validate();
    }
    public static List<String> choices(String key) {
        return switch(key){case "backgroundStyle"->List.of("rounded","solid","gradient","outline","vanilla");case "filterMode"->List.of("all","only listed","except listed");default->List.of();};
    }
    public static Set<String> itemIds(String text){var ids=new LinkedHashSet<String>();for(String part:text.split("[,;\\s]+")){String id=part.trim();if(id.isEmpty())continue;if(!id.contains(":"))id="minecraft:"+id;if(id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))ids.add(id);}return ids;}
    public double itemScale(String id,String context) {
        String mode=text("items","filterMode");boolean listed=itemIds(text("items","filterItems")).contains(id);
        if(mode.equals("only listed")&&!listed||mode.equals("except listed")&&listed)return 1;

        for(String part:text("items","overrides").split(";")) {
            String[] pair=part.trim().split("=");
            if(pair.length==2 && pair[0].trim().equals(id)) try { double x=Double.parseDouble(pair[1].trim()); if(Double.isFinite(x)) return Math.clamp(x,0.1,3); } catch(NumberFormatException ignored) {}
        }
        return number("items",context);
    }
    private synchronized void validate() {
        if(!values.has("_v1")){
            if(values.has("crosshair")&&values.get("crosshair").isJsonObject()){
                var cross=values.getAsJsonObject("crosshair");
                if(cross.has("color")&&cross.get("color").isJsonPrimitive()&&cross.get("color").getAsString().replace("#","").equalsIgnoreCase("A5F078")){cross.addProperty("color","FFFFFF");cross.addProperty("outline",false);}
            }
            values.addProperty("_v1",true);
        }
        values.remove("skins3d");
        values.remove("spotify"); // Removed module; also clean old profile snapshots.
        for(Module module:MODULES) {
            if(!values.has(module.id())||!values.get(module.id()).isJsonObject()) values.add(module.id(),new JsonObject());
            JsonObject obj=values.getAsJsonObject(module.id());
            for(Setting setting:module.settings()) {
                try {
                    JsonElement v=obj.get(setting.key()); if(v==null||!v.isJsonPrimitive()) throw new IllegalArgumentException();
                    if(setting.initial() instanceof Boolean && !v.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException();
                    if(setting.initial() instanceof Number) { double d=v.getAsDouble(); if(!Double.isFinite(d)) throw new IllegalArgumentException(); obj.addProperty(setting.key(),Math.clamp(d,setting.min(),setting.max())); }
                    if(setting.initial() instanceof String && !v.getAsJsonPrimitive().isString()) throw new IllegalArgumentException();
                } catch(Exception e) { obj.add(setting.key(),new Gson().toJsonTree(setting.initial())); }
            }
        }
    }
}

