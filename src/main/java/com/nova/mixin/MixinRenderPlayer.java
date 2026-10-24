package com.nova.mixin;

import com.nova.emote.EmotePlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Emotes that move the whole body (headspin, twerk, spin) turn and tilt the player here. */
@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {
    @Inject(method = "rotateCorpse(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", at = @At("TAIL"))
    private void nova$emoteBody(AbstractClientPlayer player, float ageInTicks, float bodyYaw, float partialTicks, CallbackInfo ci) {
        EmotePlayer.moveBody(player);
    }
}
