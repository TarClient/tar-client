package dev.tarclient.mod.mixin;
import dev.tarclient.mod.StreamerMode;
import dev.tarclient.config.StreamerCoordinates;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.List;
@Mixin(DebugHud.class)
public abstract class StreamerDebugMixin {
    @ModifyVariable(method="drawText",at=@At("HEAD"),argsOnly=true)
    private List<String> tar$coordinates(List<String> lines){
        return StreamerMode.active()?lines.stream().map(s->StreamerCoordinates.vanilla(s,StreamerMode.position())).toList():lines;
    }
}
