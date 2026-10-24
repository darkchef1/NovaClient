package com.nova.mixin;

import com.nova.module.impl.Freelook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    /** Freelook: mouse movement turns the camera instead of your character. */
    @Inject(method = "setAngles", at = @At("HEAD"), cancellable = true)
    private void nova$freelook(float yaw, float pitch, CallbackInfo ci) {
        if (Freelook.isActive() && (Object) this == Minecraft.getMinecraft().thePlayer) {
            Freelook.turn(yaw, pitch);
            ci.cancel();
        }
    }
}
