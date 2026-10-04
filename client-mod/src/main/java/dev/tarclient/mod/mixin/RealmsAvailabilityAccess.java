package dev.tarclient.mod.mixin;
import net.minecraft.client.realms.RealmsAvailability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.concurrent.CompletableFuture;
@Mixin(RealmsAvailability.class)
public interface RealmsAvailabilityAccess {
    @Accessor("currentFuture") static void tar$clear(CompletableFuture<RealmsAvailability.Info> value){throw new AssertionError();}
}
