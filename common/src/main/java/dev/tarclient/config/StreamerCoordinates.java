package dev.tarclient.config;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

/** Display-only coordinates. Never accepts a real position, so offsets cannot leak it. */
public final class StreamerCoordinates {
    private static final Pattern POSITION_LINE=Pattern.compile("(?i)^(?:XYZ|Block|Chunk|Chunk-relative|Section-relative|Targeted Block|Targeted Fluid|Targeted Entity|Looking at|Position|Player position)\\s*:.*");
    public record Position(int x,int y,int z) {
        public String block(){return x+" "+y+" "+z;}
        public String xyz(){return String.format(Locale.ROOT,"%.3f / %.5f / %.3f",(double)x,(double)y,(double)z);}
        public String chunk(){return Math.floorDiv(x,16)+" "+Math.floorDiv(y,16)+" "+Math.floorDiv(z,16);}
        public String relative(){return Math.floorMod(x,16)+" "+Math.floorMod(y,16)+" "+Math.floorMod(z,16);}
    }
    public static Position random(){var r=new SecureRandom();return new Position(r.nextInt(200001)-100000,64+r.nextInt(100),r.nextInt(200001)-100000);}
    public static String vanilla(String line,Position p){
        String plain=line.replaceAll("\u00a7.","").strip();
        if(!POSITION_LINE.matcher(plain).matches())return line;
        String key=plain.substring(0,plain.indexOf(':'));
        String value=switch(key.toLowerCase(Locale.ROOT)){
            case "xyz"->p.xyz();case "chunk"->p.chunk();
            case "section-relative","chunk-relative"->p.relative();default->p.block();
        };
        return key+": "+value+" [Streamer]";
    }
    public static String betterF3(String id,Position p){return switch(id){
        case "player_coords"->"XYZ: "+p.xyz()+" [Streamer]";
        case "block_coords"->"Block: "+p.block()+" [Streamer]";
        case "chunk_coords"->"Chunk: "+p.chunk()+" [Streamer]";
        case "chunk_relative_coords"->"Section-relative: "+p.relative()+" [Streamer]";
        case "targeted_block","targeted_fluid","targeted_entity"->"Target position: "+p.block()+" [Streamer]";
        default->null;
    };}
}
