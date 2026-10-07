package dev.tarclient.mod.mixin;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayNetworkHandler.class)
public class TpsMixin {
    @Inject(method="onWorldTimeUpdate",at=@At("TAIL"))
    private void tar$tps(WorldTimeUpdateS2CPacket packet,CallbackInfo ci){dev.tarclient.mod.ExtraHud.time(packet.time());}
}
