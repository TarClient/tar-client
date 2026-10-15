package dev.tarclient.mod.mixin;
import dev.tarclient.mod.SocialRuntime;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BipedEntityModel.class)
public class EmoteModelMixin {
    @Inject(method="setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",at=@At("TAIL"))
    private void tar$emote(BipedEntityRenderState input,CallbackInfo ci){
        if(!(input instanceof PlayerEntityRenderState state))return;
        var p=SocialRuntime.pose(state);if(p==null)return;var m=(BipedEntityModel<?>)(Object)this;
        m.head.pitch=p.head;m.head.yaw=0;m.head.roll=0;m.body.pitch=p.body;m.body.yaw=0;m.body.roll=0;
        tar$angles(m.leftArm,p.leftArm,p.leftYaw,p.leftRoll);tar$angles(m.rightArm,p.rightArm,p.rightYaw,p.rightRoll);
        tar$angles(m.leftLeg,p.leftLeg,0,0);tar$angles(m.rightLeg,p.rightLeg,0,0);
        // 1.21.11 overlay parts are children and inherit their parent's pose.
    }
    private static void tar$angles(ModelPart part,float pitch,float yaw,float roll){part.pitch=pitch;part.yaw=yaw;part.roll=roll;}
}
