package dev.tarclient.mod;

import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import static dev.tarclient.mod.TarClient.CONFIG;

/** Replaces the corner icons in their vanilla rendering pass, avoiding duplicate panels. */
public final class PotionHud {
    public static void render(DrawContext c){
        var mc=MinecraftClient.getInstance();
        if(mc.player==null||mc.options.hudHidden||mc.currentScreen!=null&&mc.currentScreen.showsStatusEffects())return;
        var effects=mc.player.getStatusEffects().stream().filter(StatusEffectInstance::shouldShowIcon)
            .filter(e->CONFIG.bool("potions",e.getEffectType().value().isBeneficial()?"beneficial":"harmful"))
            .sorted(Comparator.reverseOrder()).toList();
        float scale=CONFIG.f("potions","scale");int margin=CONFIG.i("potions","margin");
        int columns=Math.max(1,(int)((c.getScaledWindowWidth()-2*margin)/(26*scale)));
        int goodCount=(int)effects.stream().filter(e->e.getEffectType().value().isBeneficial()).count();
        int goodRows=(goodCount+columns-1)/columns,good=0,bad=0;
        boolean duration=CONFIG.bool("potions","duration");int rowHeight=duration?35:26;
        for(var effect:effects){
            boolean beneficial=effect.getEffectType().value().isBeneficial();int index=beneficial?good++:bad++;
            int column=index%columns,row=index/columns+(beneficial?0:goodRows);
            float x=c.getScaledWindowWidth()-margin-(column*26+24)*scale;
            float y=margin+(mc.isDemo()?15:0)+row*rowHeight*scale;
            c.getMatrices().pushMatrix();c.getMatrices().translate(x,y);c.getMatrices().scale(scale,scale);
            if(CONFIG.bool("potions","showBackground"))c.drawGuiTexture(RenderPipelines.GUI_TEXTURED,Identifier.ofVanilla(effect.isAmbient()?"hud/effect_background_ambient":"hud/effect_background"),0,0,24,24);
            c.drawGuiTexture(RenderPipelines.GUI_TEXTURED,InGameHud.getEffectTexture(effect.getEffectType()),3,3,18,18);
            if(CONFIG.bool("potions","amplifier")&&effect.getAmplifier()>0){String level=Integer.toString(effect.getAmplifier()+1);c.drawTextWithShadow(mc.textRenderer,level,23-mc.textRenderer.getWidth(level),1,0xFFFFFFFF);}
            if(duration){int seconds=Math.max(0,(effect.getDuration()+19)/20);String time=effect.isInfinite()?"∞":String.format(Locale.ROOT,"%d:%02d",seconds/60,seconds%60);
                int color=CONFIG.color("potions",!effect.isInfinite()&&seconds<=10?"expiringColor":"color");
                float textScale=Math.min(0.8f,24f/Math.max(1,mc.textRenderer.getWidth(time)));
                c.getMatrices().pushMatrix();c.getMatrices().translate(12,25);c.getMatrices().scale(textScale,textScale);c.drawTextWithShadow(mc.textRenderer,time,-mc.textRenderer.getWidth(time)/2,0,color);c.getMatrices().popMatrix();
            }
            c.getMatrices().popMatrix();
        }
    }
}
