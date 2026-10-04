package dev.tarclient.mod.mixin;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(SplashTextResourceSupplier.class)
public interface SplashSessionAccess {
    @Mutable @Accessor("session") void tar$session(Session session);
}
