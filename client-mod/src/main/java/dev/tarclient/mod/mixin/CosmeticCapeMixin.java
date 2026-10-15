package dev.tarclient.mod.mixin;
import dev.tarclient.mod.SocialRuntime;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CapeFeatureRenderer.class)
public class CosmeticCapeMixin {
    @Inject(method="render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V",at=@At("HEAD"),cancellable=true)
    private void tar$cape(MatrixStack m,OrderedRenderCommandQueue q,int light,PlayerEntityRenderState state,float yaw,float pitch,CallbackInfo ci){var look=SocialRuntime.look(state);if(look!=null&&look.cosmetics().stream().anyMatch(id->id.endsWith("cape")))ci.cancel();}
}
