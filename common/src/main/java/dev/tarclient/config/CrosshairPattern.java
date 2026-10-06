package dev.tarclient.config;

/** Portable 15 by 15 bitmap shared by both editors and profiles. */
public final class CrosshairPattern {
    public static final int SIZE=15, PIXELS=SIZE*SIZE;
    private CrosshairPattern() {}
    public static String normalize(String value){
        if(value==null||value.length()!=PIXELS||!value.matches("[01]+"))return vanilla();
        return value;
    }
    public static String vanilla(){
        char[] pixels="0".repeat(PIXELS).toCharArray();
        for(int i=2;i<SIZE-2;i++){pixels[7*SIZE+i]='1';pixels[i*SIZE+7]='1';}
        return new String(pixels);
    }
    public static boolean pixel(String pattern,int x,int y){return x>=0&&y>=0&&x<SIZE&&y<SIZE&&pattern.charAt(y*SIZE+x)=='1';}
    public static String paint(String pattern,int x,int y,boolean on){
        pattern=normalize(pattern);if(x<0||y<0||x>=SIZE||y>=SIZE)return pattern;
        char[] data=pattern.toCharArray();data[y*SIZE+x]=on?'1':'0';return new String(data);
    }
}
