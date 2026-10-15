package dev.tarclient.config;
import com.google.gson.*;
import java.util.*;
public record SocialState(List<Person> people,List<String> cosmetics) {
    public static final SocialState EMPTY=new SocialState(List.of(),List.of());
    public record Person(String uuid,String name,String kind,boolean online){
        @Override public String toString(){return name+"  · "+(kind.equals("friend")?(online?"Online":"Offline"):kind);}
    }
    public static SocialState parse(JsonObject json){
        var people=new ArrayList<Person>();
        for(var e:json.getAsJsonArray("relations")){var p=e.getAsJsonObject();people.add(new Person(p.get("uuid").getAsString(),p.get("name").getAsString(),p.get("kind").getAsString(),p.get("online").getAsBoolean()));}
        for(var e:json.getAsJsonArray("blocked")){var p=e.getAsJsonObject();people.add(new Person(p.get("uuid").getAsString(),p.get("name").getAsString(),"blocked",false));}
        return new SocialState(List.copyOf(people),strings(json.getAsJsonArray("look")));
    }
    public static List<String> strings(JsonArray array){var list=new ArrayList<String>();for(var e:array)list.add(e.getAsString());return List.copyOf(list);}
    public static JsonObject equipment(List<String> ids){var b=new JsonObject();var a=new JsonArray();ids.forEach(a::add);b.add("cosmetics",a);return b;}
    public record Message(String sender,String text,long created){}
    public static List<Message> messages(JsonObject json){var list=new ArrayList<Message>();for(var e:json.getAsJsonArray("messages")){var m=e.getAsJsonObject();list.add(new Message(m.get("sender").getAsString(),m.get("text").getAsString(),m.get("created").getAsLong()));}return List.copyOf(list);}
}
