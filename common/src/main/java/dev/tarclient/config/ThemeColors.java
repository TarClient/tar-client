package dev.tarclient.config;

/** RGB menu colors shared by the desktop launcher and in-game menu. */
public final class ThemeColors {
    public static final String DEFAULT_ACCENT="E78336", DEFAULT_BACKGROUND="0B0B0D";
    private ThemeColors() {}
    public static boolean valid(String value) { return value!=null&&value.trim().matches("#?[0-9a-fA-F]{6}"); }
    public static int parse(String value,String fallback) {
        String hex=valid(value)?value.trim():fallback;
        return Integer.parseInt(hex.replace("#",""),16);
    }
    private static double linear(int c){double v=c/255.0;return v<=0.04045?v/12.92:Math.pow((v+0.055)/1.055,2.4);}
    public static double luminance(int rgb){return 0.2126*linear((rgb>>>16)&255)+0.7152*linear((rgb>>>8)&255)+0.0722*linear(rgb&255);}
    /** Pick the higher-contrast label color, including for very dark custom accents. */
    public static int foreground(int rgb){return luminance(rgb)>0.179?0xFF000000:0xFFFFFFFF;}
    public static int blend(int from,int to,double amount){
        double t=Math.clamp(amount,0,1);int result=0;
        for(int shift:new int[]{16,8,0})result|=(int)Math.round(((from>>>shift)&255)*(1-t)+((to>>>shift)&255)*t)<<shift;
        return result;
    }
    public static int menu(int original,boolean dark,String accentHex,String backgroundHex){
        int alpha=original&0xFF000000,rgb=original&0xFFFFFF;
        int accent=parse(accentHex,DEFAULT_ACCENT),base=dark?parse(backgroundHex,DEFAULT_BACKGROUND):0xF3F3F5;
        int ink=foreground(base)&0xFFFFFF;
        int mapped=switch(rgb){
            case 0xB9F47A,0xA5F078 -> accent;
            case 0x142019 -> foreground(accent)&0xFFFFFF;
            case 0x080C12 -> dark?0x030304:0xDADADD;
            case 0x101720 -> base;
            case 0x1A2430,0x1C222A -> blend(base,ink,0.045);
            case 0x252E3B,0x273240 -> blend(base,ink,0.085);
            case 0x293341 -> blend(base,ink,0.18);
            case 0x354252,0x344150,0x52684C -> blend(base,ink,0.15);
            case 0x516074,0x637082 -> blend(base,ink,0.48);
            case 0x8190A2,0x8D9BAC,0x9DADBF -> blend(base,ink,0.68);
            case 0xEAF0F6,0xE8EDF5,0xF0F5FA -> blend(base,ink,0.94);
            default -> rgb;
        };
        return alpha|mapped;
    }
}
