package com.nova.mixin;

import com.nova.NovaClient;
import com.nova.module.impl.Freelook;
import com.nova.module.impl.NoHurtShake;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    /** No Hurt Shake: skip the camera shake when you take damage. */
    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void nova$noHurtShake(float partialTicks, CallbackInfo ci) {
        if (NovaClient.isEnabled(NoHurtShake.class)) ci.cancel();
    }

    // Freelook: while active, the camera reads the freelook angles instead of the player's.

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F", opcode = Opcodes.GETFIELD))
    private float nova$yaw(Entity entity) {
        return Freelook.yaw(entity, entity.rotationYaw);
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F", opcode = Opcodes.GETFIELD))
    private float nova$prevYaw(Entity entity) {
        return Freelook.yaw(entity, entity.prevRotationYaw);
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F", opcode = Opcodes.GETFIELD))
    private float nova$pitch(Entity entity) {
        return Freelook.pitch(entity, entity.rotationPitch);
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F", opcode = Opcodes.GETFIELD))
    private float nova$prevPitch(Entity entity) {
        return Freelook.pitch(entity, entity.prevRotationPitch);
    }
}
