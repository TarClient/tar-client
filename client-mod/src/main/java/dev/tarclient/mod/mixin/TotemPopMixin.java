package dev.tarclient.mod.mixin;

import dev.tarclient.mod.TarClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameOverlayRenderer.class)
public class TotemPopMixin {
    @Shadow private ItemStack floatingItem;

    // Scale inside vanilla's pushed matrix, after translation. The animation's
    // position, duration, rotation, and other overlays keep their vanilla behavior.
    @ModifyArgs(method="renderFloatingItem", at=@At(value="INVOKE",
        target="Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void tar$totemSize(Args args) {
        if (floatingItem == null || !floatingItem.isOf(Items.TOTEM_OF_UNDYING)
            || !TarClient.CONFIG.on("totempop")) return;
        float multiplier = TarClient.CONFIG.f("totempop", "size") / 100.0f;
        for (int axis = 0; axis < 3; axis++) args.set(axis, (float) args.get(axis) * multiplier);
    }
}
