package dev.tarclient.mod.mixin;
import dev.tarclient.mod.SocialRuntime;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEntityRenderer.class)
public class EmoteTransformMixin {
    @Inject(method="setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",at=@At("TAIL"))
    private void tar$pose(PlayerEntityRenderState state,MatrixStack matrices,float bodyYaw,float scale,CallbackInfo ci){
        var p=SocialRuntime.pose(state);if(p==null)return;
        matrices.translate(0,p.lift,p.lean);matrices.multiply(RotationAxis.POSITIVE_Y.rotation(p.turn));matrices.multiply(RotationAxis.POSITIVE_X.rotation(p.tilt));matrices.multiply(RotationAxis.POSITIVE_Z.rotation(p.roll));
    }
}
