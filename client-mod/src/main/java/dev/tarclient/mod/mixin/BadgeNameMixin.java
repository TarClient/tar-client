package dev.tarclient.mod.mixin;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityRenderer.class)
public class BadgeNameMixin {
    @Inject(method="updateRenderState",at=@At("TAIL"))
    private void tar$badge(Entity entity,EntityRenderState state,float delta,CallbackInfo ci){if(entity instanceof PlayerEntity&&state.displayName!=null)state.displayName=dev.tarclient.mod.CommunityBadges.decorate(entity.getUuid(),state.displayName);}
}
