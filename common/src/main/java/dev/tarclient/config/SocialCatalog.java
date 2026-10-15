package dev.tarclient.config;
import java.util.*;
public final class SocialCatalog {
    public record Item(String id,String name,String slot){@Override public String toString(){return name+" · Free";}}
    public static final List<Item> EMOTES=List.of(
        new Item("lean","Michael Jackson Lean","emote"),new Item("moonwalk","Moonwalk","emote"),new Item("backflip","Backflip","emote"),new Item("ironman","Iron-Man","emote"),
        new Item("ratdance","Rat dance","emote"),new Item("worm","Worm","emote"),new Item("sit","Sit","emote"),new Item("lay","Lay","emote"),new Item("dab","Dab","emote"),new Item("wave","Wave","emote"),new Item("animerun","Anime run","emote"),new Item("tpose","T-Pose","emote"),new Item("clap","Clap","emote"),new Item("ascend","Ascend","emote"),new Item("griddy","Griddy","emote"),new Item("o7","o7 salute","emote"));
    public static final List<Item> COSMETICS=List.of(
        new Item("angelwings","Angel Wings","wings"),new Item("demonwings","Demon Wings","wings"),new Item("fairywings","Fairy Wings","wings"),new Item("butterflywings","Butterfly Wings","wings"),
        new Item("demonhorns","Demon Horns","head"),new Item("angelhalo","Angel Halo","head"),new Item("purplehalo","Purple Halo","head"),new Item("greenhalo","Green Halo","head"),new Item("bluehalo","Blue Halo","head"),
        new Item("katana","Katana","hip"),new Item("blackbandana","Black Bandana","face"),new Item("whitebandana","White Bandana","face"),new Item("basicbandana","Basic Bandana","face"),new Item("greatsword","GreatSword","back"),new Item("basiccap","Basic Cap","head"),
        new Item("oceancape","Ocean Cape","back"),new Item("lightningcape","White Lightning Cape","back"),new Item("blossomcape","Blossom Cape","back"),new Item("nethercape","Nether Cape","back"),new Item("basiccape","Basic Cape","back"));
    public static List<String> equip(List<String> current,String id){
        var item=COSMETICS.stream().filter(i->i.id().equals(id)).findFirst().orElseThrow();var result=new ArrayList<String>();
        for(String old:current)if(COSMETICS.stream().anyMatch(i->i.id().equals(old)&&!i.slot().equals(item.slot())))result.add(old);
        if(!current.contains(id))result.add(id);return List.copyOf(result);
    }
    public static Item emote(String id){return EMOTES.stream().filter(i->i.id().equals(id)).findFirst().orElse(null);}
}
