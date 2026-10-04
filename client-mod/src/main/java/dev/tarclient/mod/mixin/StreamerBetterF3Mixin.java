package dev.tarclient.mod.mixin;
import dev.tarclient.mod.StreamerMode;
import dev.tarclient.config.StreamerCoordinates;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="me.cominixo.betterf3.utils.DebugLine",remap=false)
public abstract class StreamerBetterF3Mixin {
    @Shadow @Final private String id;
    @Inject(method="toText",at=@At("HEAD"),cancellable=true,remap=false)
    private void tar$coordinates(TextColor nameColor,TextColor valueColor,CallbackInfoReturnable<Text> cir){
        if(StreamerMode.active()){String masked=StreamerCoordinates.betterF3(id,StreamerMode.position());if(masked!=null)cir.setReturnValue(Text.literal(masked).styled(s->s.withColor(valueColor)));}
    }
    @Inject(method="toTextCustom",at=@At("HEAD"),cancellable=true,remap=false)
    private void tar$customCoordinates(TextColor color,CallbackInfoReturnable<Text> cir){
        if(StreamerMode.active()){String masked=StreamerCoordinates.betterF3(id,StreamerMode.position());if(masked!=null)cir.setReturnValue(Text.literal(masked).styled(s->s.withColor(color)));}
    }
}
