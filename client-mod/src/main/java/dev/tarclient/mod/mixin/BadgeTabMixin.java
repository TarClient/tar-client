package dev.tarclient.mod.mixin;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerListHud.class)
public class BadgeTabMixin {
    @Inject(method="getPlayerName",at=@At("RETURN"),cancellable=true)
    private void tar$badge(PlayerListEntry entry,CallbackInfoReturnable<Text> ci){ci.setReturnValue(dev.tarclient.mod.CommunityBadges.decorate(entry.getProfile().id(),ci.getReturnValue()));}
}
