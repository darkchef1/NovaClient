package com.nova.mixin;

import com.nova.cosmetic.CapeArt;
import com.nova.cosmetic.CosmeticSlot;
import com.nova.cosmetic.Cosmetics;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Capes: Minecraft asks each player for their cape texture; Nova players answer with their Nova cape.
 * Minecraft then draws and swings it exactly like a normal cape.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {
    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    private void nova$cape(CallbackInfoReturnable<ResourceLocation> cir) {
        Map<CosmeticSlot, String> set = Cosmetics.forPlayer((AbstractClientPlayer) (Object) this);
        String cape = set == null ? null : set.get(CosmeticSlot.CAPE);
        if (cape != null) cir.setReturnValue(CapeArt.texture(cape));
    }
}
