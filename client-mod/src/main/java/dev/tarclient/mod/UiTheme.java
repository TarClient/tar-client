package dev.tarclient.mod;
public final class UiTheme {
    public static int color(int value){if(TarClient.CONFIG.on("darkmode"))return value;int alpha=value&0xff000000,r=(value>>>16)&255,g=(value>>>8)&255,b=value&255;
        if(g>r*1.12&&g>b*1.12)return alpha|0x347348;
        int light=(r+g+b)/3;return alpha|(light<65?0xEDF2F8:light<150?0x52647A:0x202E42);
    }
}
