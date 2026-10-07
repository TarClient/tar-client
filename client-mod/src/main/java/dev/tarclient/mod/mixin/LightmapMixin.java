package dev.tarclient.mod.mixin;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import dev.tarclient.mod.TarClient;
import org.joml.Vector3f; import org.joml.Vector3fc;
@Mixin(LightmapTextureManager.class)
public class LightmapMixin {
    // AmbientLightFactor blends every lightmap cell toward white, even block/sky level zero.
    @ModifyArg(method="update",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",ordinal=0),index=0)
    private float tar$ambient(float original){return TarClient.CONFIG.on("fullbright")?Math.max(original,TarClient.CONFIG.f("fullbright","strength")):original;}
    @ModifyArg(method="update",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/buffers/Std140Builder;putVec3(Lorg/joml/Vector3fc;)Lcom/mojang/blaze3d/buffers/Std140Builder;",ordinal=1),index=0)
    private Vector3fc tar$white(Vector3fc original){return TarClient.CONFIG.on("fullbright")?new Vector3f(1,1,1):original;}
}

