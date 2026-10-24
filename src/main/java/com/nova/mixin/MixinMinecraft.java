package com.nova.mixin;

import com.nova.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla caps menus at 30 FPS when you're not in a world, which makes the animated
 * main menu look choppy. Use the "Max main menu FPS" setting instead (like Lunar).
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow public WorldClient theWorld;
    @Shadow public GuiScreen currentScreen;

    @Inject(method = "getLimitFramerate", at = @At("HEAD"), cancellable = true)
    private void nova$menuFps(CallbackInfoReturnable<Integer> cir) {
        if (theWorld == null && currentScreen != null && NovaClient.getSettings() != null) {
            cir.setReturnValue(NovaClient.getSettings().menuFps.getInt());
        }
    }
}
