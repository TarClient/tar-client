package dev.tarclient.launcher;

import dev.tarclient.config.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

class Release102Test {
    @TempDir Path dir;
    public static class AppleConfig {
        public boolean showSaturationHudOverlay,showFoodValuesHudOverlay,showFoodValuesHudOverlayWhenOffhand;
        public boolean showFoodExhaustionHudUnderlay,showFoodHealthHudOverlay,showFoodValuesInTooltip,showVanillaAnimationsOverlay;
        public boolean showFoodValuesInTooltipAlways=true;
        public float maxHudOverlayFlashAlpha;
    }
    @Test void disablingSaturationHidesEveryOverlayAndReenablingRestoresPreferences()throws Exception {
        var config=new ClientConfig();config.set("saturation","enabled",true);config.set("saturation","exhaustion",true);
        config.set("saturation","healthPreview",true);config.set("saturation","tooltips",true);config.set("saturation","opacity",40);
        var upstream=new AppleConfig();var binding=new AppleSkinSettings.Binding(upstream);binding.apply(config);
        assertTrue(upstream.showSaturationHudOverlay);assertTrue(upstream.showFoodValuesHudOverlay);
        assertTrue(upstream.showFoodExhaustionHudUnderlay);assertEquals(0.4f,upstream.maxHudOverlayFlashAlpha,0.001f);
        config.set("saturation","enabled",false);binding.apply(config);
        assertFalse(upstream.showSaturationHudOverlay);assertFalse(upstream.showFoodValuesHudOverlay);
        assertFalse(upstream.showFoodValuesHudOverlayWhenOffhand);assertFalse(upstream.showFoodExhaustionHudUnderlay);
        assertFalse(upstream.showFoodHealthHudOverlay);assertFalse(upstream.showFoodValuesInTooltip);
        assertTrue(upstream.showFoodValuesInTooltipAlways); // Leave unrelated upstream preferences intact.
        config.set("saturation","enabled",true);binding.apply(config);
        assertTrue(upstream.showFoodHealthHudOverlay);assertTrue(upstream.showFoodValuesInTooltip);
        assertTrue(upstream.showFoodExhaustionHudUnderlay);
    }
    @Test void oldSaturationProfilesKeepTheirEnabledStateAndAcquireHungerBarOptions()throws Exception {
        Path old=dir.resolve("legacy.json");Files.writeString(old,"{\"saturation\":{\"enabled\":true,\"x\":81,\"y\":12,\"scale\":2}}");
        var config=ClientConfig.read(old);assertTrue(config.on("saturation"));assertTrue(config.bool("saturation","foodPreview"));
        config.set("saturation","offhand",false);config.set("saturation","opacity",55);
        var profiles=new ProfileStore(dir);profiles.create("Hunger",config);var loaded=profiles.load("Hunger");
        var upstream=new AppleConfig();new AppleSkinSettings.Binding(upstream).apply(loaded);
        assertTrue(upstream.showSaturationHudOverlay);assertFalse(upstream.showFoodValuesHudOverlayWhenOffhand);
        assertEquals(0.55f,upstream.maxHudOverlayFlashAlpha,0.001f);
        var module=ClientConfig.MODULES.stream().filter(m->m.id().equals("saturation")).findFirst().orElseThrow();
        assertTrue(module.settings().stream().noneMatch(s->s.key().equals("x")||s.key().equals("y")));
    }
    @Test void anExistingDisabledAppleSkinIsEnabledWithoutDuplicateDownloads()throws Exception {
        var mods=new ModManager(dir,s->{});Path file=dir.resolve("mods/appleskin.jar.disabled");
        try(var zip=new ZipOutputStream(Files.newOutputStream(file))){zip.putNextEntry(new ZipEntry("fabric.mod.json"));zip.write("{\"id\":\"appleskin\",\"version\":\"3.0.8\",\"environment\":\"*\",\"depends\":{\"minecraft\":\"1.21.11\"}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));zip.closeEntry();}
        mods.ensureSaturationRenderer();mods.ensureSaturationRenderer();
        assertEquals(1,mods.list().size());assertEquals("appleskin.jar",mods.list().getFirst().getFileName().toString());mods.preflight();
    }
}
