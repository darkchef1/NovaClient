package com.nova.mixin;

import com.nova.module.impl.OldAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.7 Animations: in 1.8, holding right-click positions the item with no swing (swing = 0).
 * In renderItemInFirstPerson, transformFirstPersonItem is called once per use action:
 * ordinal 1 = eating/drinking, 2 = blocking, 3 = bow. Passing the real swing brings back the 1.7 look.
 */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
    private static final String TRANSFORM = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V";
    private float nova$partialTicks;

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void nova$capturePartialTicks(float partialTicks, CallbackInfo ci) {
        nova$partialTicks = partialTicks;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = TRANSFORM, ordinal = 1), index = 1)
    private float nova$eatSwing(float swing) {
        return OldAnimations.swingWhileUsing() ? swingProgress() : swing;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = TRANSFORM, ordinal = 2), index = 1)
    private float nova$blockSwing(float swing) {
        return OldAnimations.blockhit() ? swingProgress() : swing;
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = TRANSFORM, ordinal = 3), index = 1)
    private float nova$bowSwing(float swing) {
        return OldAnimations.swingWhileUsing() ? swingProgress() : swing;
    }

    private float swingProgress() {
        return Minecraft.getMinecraft().thePlayer.getSwingProgress(nova$partialTicks);
    }
}
