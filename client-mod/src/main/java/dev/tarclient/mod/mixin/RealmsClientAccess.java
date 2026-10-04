package dev.tarclient.mod.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.RealmsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
@Mixin(RealmsClient.class)
public interface RealmsClientAccess {
    @Invoker("<init>") static RealmsClient tar$create(String sessionId,String name,MinecraftClient client){throw new AssertionError();}
    @Accessor("instance") static void tar$setInstance(RealmsClient client){throw new AssertionError();}
}
