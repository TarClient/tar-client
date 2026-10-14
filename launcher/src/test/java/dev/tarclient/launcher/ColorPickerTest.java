package dev.tarclient.launcher;

import dev.tarclient.config.ClientConfig;
import dev.tarclient.config.ColorPickerModel;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ColorPickerTest {
    @Test void hsvRoundTripKeepsStoredColorsAndOpacity(){
        for(int r=0;r<=255;r+=17)for(int g=0;g<=255;g+=17)for(int b=0;b<=255;b+=17){
            int rgb=(r<<16)|(g<<8)|b;String text=String.format(Locale.ROOT,"%06X",rgb);
            assertEquals(text,new ColorPickerModel(text,false).hex());
            assertEquals("7A"+text,new ColorPickerModel("7A"+text,true).hex());
        }
    }
    @Test void invalidOrPartialHexDoesNotDestroyCurrentChoice(){
        var model=new ColorPickerModel("B8181D29",true);
        for(String input:new String[]{"","#","ABC","FFGGGG","1234567","FFFFFFFFF",null}){
            assertFalse(model.setHex(input));assertEquals("B8181D29",model.hex());
        }
        assertTrue(model.setHex(" #abCDef "));assertEquals("FFABCDEF",model.hex());
    }
    @Test void opacityEditsAndRgbSettingsUseCorrectFormat(){
        var rgba=new ColorPickerModel("AABBCC",true);rgba.alpha(0);assertEquals("00AABBCC",rgba.hex());
        rgba.alpha(0.5);assertEquals("80AABBCC",rgba.hex());
        rgba.alpha(2);assertEquals("FFAABBCC",rgba.hex());
        var rgb=new ColorPickerModel("AABBCC",false);rgb.alpha(0);assertEquals("AABBCC",rgb.hex());assertEquals(0xFFAABBCC,rgb.argb());
        assertFalse(rgb.setHex("80AABBCC"));
    }
    @Test void gradientCornersAndHueWrapAreCorrect(){
        assertEquals(0xFFFFFF,ColorPickerModel.rgb(0.5,0,1));
        assertEquals(0x000000,ColorPickerModel.rgb(0.5,1,0));
        assertEquals(0x00FFFF,ColorPickerModel.rgb(0.5,1,1));
        assertEquals(ColorPickerModel.rgb(0,1,1),ColorPickerModel.rgb(1,1,1));
        var model=new ColorPickerModel("FF0000",false);model.saturationValue(-1,2);assertEquals("FFFFFF",model.hex());
    }
    @Test void grayAndBlackKeepHueForFurtherDragging(){
        var model=new ColorPickerModel("00FF00",false);double hue=model.hue();
        model.setHex("808080");assertEquals(hue,model.hue());
        model.setHex("000000");assertEquals(hue,model.hue());
        model.saturationValue(1,1);assertEquals("00FF00",model.hex());
    }
    @Test void everyExistingColorSettingHasPickerMetadataAndThemeKeepsItsSavedId(){
        Set<String> keys=Set.of("color","background","backgroundEnd","pressed","expiringColor","accent","menuBackground");
        var config=new ClientConfig();int colors=0;
        for(var module:ClientConfig.MODULES)for(var setting:module.settings()){
            assertEquals(keys.contains(setting.key()),setting.color(),module.id()+"."+setting.key());
            if(setting.color()){
                colors++;String stored=config.text(module.id(),setting.key());
                assertTrue(ColorPickerModel.valid(stored,setting.alpha()));
                assertEquals(stored,new ColorPickerModel(stored,setting.alpha()).hex());
            }
        }
        assertTrue(colors>40);
        assertEquals("Theme",ClientConfig.MODULES.stream().filter(m->m.id().equals("darkmode")).findFirst().orElseThrow().name());
    }
    @Test void desktopPickerOpensOnGradientAndPreservesAlpha() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var chooser=ColorPickerDialog.createChooser("7A181D29","FFFFFFFF",true);
            assertEquals(0x7A181D29,chooser.getColor().getRGB());
            assertEquals(1,chooser.getChooserPanels().length);
            assertEquals("HSV",chooser.getChooserPanels()[0].getDisplayName());
            assertTrue(chooser.getChooserPanels()[0].isColorTransparencySelectionEnabled());
            var rgb=ColorPickerDialog.createChooser("E78336","FFFFFF",false);
            assertEquals(255,rgb.getColor().getAlpha());assertFalse(rgb.getChooserPanels()[0].isColorTransparencySelectionEnabled());
        });
    }
}
