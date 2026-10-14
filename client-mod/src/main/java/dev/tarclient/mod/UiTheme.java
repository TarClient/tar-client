package dev.tarclient.mod;
import dev.tarclient.config.ThemeColors;
public final class UiTheme {
    public static int color(int value){
        var config=TarClient.CONFIG;
        return ThemeColors.menu(value,config.on("darkmode"),config.text("darkmode","accent"),config.text("darkmode","menuBackground"));
    }
}
