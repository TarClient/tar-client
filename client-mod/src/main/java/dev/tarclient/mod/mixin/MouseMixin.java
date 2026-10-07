package dev.tarclient.mod.mixin;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.tarclient.mod.TarClient;
@Mixin(Mouse.class)
public class MouseMixin {
    @Redirect(method="updateMouse",at=@At(value="INVOKE",target="Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void tar$look(net.minecraft.client.network.ClientPlayerEntity player,double x,double y){dev.tarclient.mod.ExtraHud.mouse(x,y);if(dev.tarclient.mod.ClientFeatures.freelooking)dev.tarclient.mod.ClientFeatures.mouse(x,y);else player.changeLookDirection(x,y);}
    @Inject(method="onMouseScroll",at=@At("HEAD"),cancellable=true)
    private void tar$scroll(long window,double horizontal,double vertical,CallbackInfo ci){if(window==MinecraftClient.getInstance().getWindow().getHandle()&&dev.tarclient.mod.ClientFeatures.scrollZoom(vertical))ci.cancel();}
    @Inject(method="onMouseButton",at=@At("HEAD"),cancellable=true)
    private void tar$click(long window,MouseInput input,int action,CallbackInfo ci){var client=MinecraftClient.getInstance();if(window!=client.getWindow().getHandle())return;
        if(TarClient.MENU_KEY!=null&&TarClient.MENU_KEY.matchesMouse(new net.minecraft.client.gui.Click(0,0,input))
            &&(client.currentScreen==null||client.currentScreen instanceof dev.tarclient.mod.TarSettingsScreen||client.currentScreen instanceof dev.tarclient.mod.HudEditorScreen)){
            if(action==1)TarClient.toggleMenu(client);TarClient.MENU_KEY.setPressed(false);ci.cancel();return;
        }
        if(action==1&&client.currentScreen==null)TarClient.click(input.button());}
}
