package dev.tarclient.config;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/** Tar controls AppleSkin's public config, without depending on Minecraft mappings. */
public final class AppleSkinSettings {
    private static final Map<String,String> OPTIONS;
    static {
        var options=new LinkedHashMap<String,String>();
        options.put("current","showSaturationHudOverlay");
        options.put("foodPreview","showFoodValuesHudOverlay");
        options.put("offhand","showFoodValuesHudOverlayWhenOffhand");
        options.put("exhaustion","showFoodExhaustionHudUnderlay");
        options.put("healthPreview","showFoodHealthHudOverlay");
        options.put("tooltips","showFoodValuesInTooltip");
        options.put("animations","showVanillaAnimationsOverlay");
        OPTIONS=Map.copyOf(options);
    }
    public static final class Binding {
        private final Object target;
        private final Map<String,Field> fields=new LinkedHashMap<>();
        private final Field opacity;
        public Binding(Object target)throws ReflectiveOperationException {
            this.target=target;
            for(var entry:OPTIONS.entrySet()){
                var field=target.getClass().getField(entry.getValue());
                if(field.getType()!=boolean.class)throw new NoSuchFieldException(entry.getValue());
                fields.put(entry.getKey(),field);
            }
            opacity=target.getClass().getField("maxHudOverlayFlashAlpha");
            if(opacity.getType()!=float.class)throw new NoSuchFieldException(opacity.getName());
        }
        public void apply(ClientConfig config)throws IllegalAccessException {
            boolean enabled=config.on("saturation");
            for(var entry:fields.entrySet())entry.getValue().setBoolean(target,enabled&&config.bool("saturation",entry.getKey()));
            opacity.setFloat(target,config.f("saturation","opacity")/100f);
        }
    }
}
