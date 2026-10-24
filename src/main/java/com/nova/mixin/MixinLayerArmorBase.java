package com.nova.mixin;

import com.nova.module.impl.OldAnimations;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 1.7 Animations: armor flashes red with the player when hit, like it did in 1.7. */
@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase {
    @Inject(method = "shouldCombineTextures", at = @At("HEAD"), cancellable = true)
    private void nova$redArmor(CallbackInfoReturnable<Boolean> cir) {
        if (OldAnimations.redArmor()) cir.setReturnValue(true);
    }
}
