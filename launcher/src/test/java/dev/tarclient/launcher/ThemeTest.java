package dev.tarclient.launcher;

import dev.tarclient.config.ClientConfig;
import dev.tarclient.config.ThemeColors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ThemeTest {
    @TempDir Path temp;
    @Test void oldSettingsGainThemeWithoutChangingHudOrLightMode() throws Exception {
        Path file=temp.resolve("settings.json");
        Files.writeString(file,"{\"darkmode\":{\"enabled\":false},\"armor\":{\"background\":\"AABBCCDD\",\"showBackground\":true}}");
        var config=ClientConfig.read(file);
        assertFalse(config.on("darkmode"));assertTrue(config.bool("armor","showBackground"));
        assertEquals("AABBCCDD",config.text("armor","background"));
        assertEquals(ThemeColors.DEFAULT_ACCENT,config.text("darkmode","accent"));
        config.set("darkmode","accent","#8040ff");config.set("darkmode","menuBackground","121010");config.save(file);
        var restored=ClientConfig.read(file);
        assertEquals("#8040ff",restored.text("darkmode","accent"));
        assertEquals("121010",restored.text("darkmode","menuBackground"));
    }
    @Test void partialOrInvalidColorEntryUsesSafeFallback() {
        for(String input:new String[]{"","#","ABC","GGGGGG","12345678","-12345",null}){
            assertFalse(ThemeColors.valid(input));
            assertEquals(0xE78336,ThemeColors.parse(input,ThemeColors.DEFAULT_ACCENT));
        }
        assertTrue(ThemeColors.valid(" #aBcD09 "));assertEquals(0xABCD09,ThemeColors.parse(" #aBcD09 ","000000"));
    }
    @Test void selectedLabelsRemainReadableAcrossCustomAccents() {
        for(int r=0;r<=255;r+=17)for(int g=0;g<=255;g+=17)for(int b=0;b<=255;b+=17){
            int rgb=(r<<16)|(g<<8)|b;double a=ThemeColors.luminance(rgb),text=ThemeColors.luminance(ThemeColors.foreground(rgb));
            assertTrue((Math.max(a,text)+0.05)/(Math.min(a,text)+0.05)>=4.5);
        }
    }
    @Test void palettePreservesTransparencyAndDoesNotRecolorRankColors() {
        assertEquals(0xFA050505,ThemeColors.menu(0xFA101720,true,"551100","050505"));
        assertEquals(0xFF551100,ThemeColors.menu(0xFFB9F47A,true,"551100","050505"));
        assertEquals(0xFFFFFFFF,ThemeColors.menu(0xFF142019,true,"551100","050505"));
        for(var rank:BadgeRank.values()){
            int color=0xFF000000|rank.color;
            assertEquals(color,ThemeColors.menu(color,true,"551100","050505"));
        }
    }
    @Test void switchingLauncherThemeRestoresDarkPalette() {
        try {
            LauncherTheme.configure(false,"8040FF","101010");
            assertTrue(ThemeColors.luminance(LauncherTheme.BG.getRGB())>0.8);
            LauncherTheme.configure(true,"#551100","050505");
            assertEquals(0xFF050505,LauncherTheme.BG.getRGB());
            assertEquals(0xFF551100,LauncherTheme.ACCENT.getRGB());
            assertEquals(0xFFFFFFFF,LauncherTheme.accentText().getRGB());
        } finally {LauncherTheme.configure(true,ThemeColors.DEFAULT_ACCENT,ThemeColors.DEFAULT_BACKGROUND);}
    }
}
