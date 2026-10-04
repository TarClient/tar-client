package dev.tarclient.mod;
import dev.tarclient.config.StreamerCoordinates;
public final class StreamerMode {
    private static StreamerCoordinates.Position position=StreamerCoordinates.random();
    private static boolean enabled;
    public static boolean active(){boolean now=TarClient.CONFIG.on("streamer");if(now&&!enabled)reroll();enabled=now;return now;}
    public static void reroll(){position=StreamerCoordinates.random();}
    public static StreamerCoordinates.Position position(){return position;}
}
