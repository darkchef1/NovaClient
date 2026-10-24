package com.nova.mixin;

import com.nova.NovaClient;
import com.nova.module.impl.ScoreboardHud;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides Minecraft's own scoreboard while Nova's movable Scoreboard is on. */
@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {
    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void nova$scoreboard(ScoreObjective objective, ScaledResolution resolution, CallbackInfo ci) {
        if (NovaClient.isEnabled(ScoreboardHud.class)) ci.cancel();
    }
}
