package com.nova.mixin;

import com.nova.NovaClient;
import com.nova.module.impl.HitColor;
import com.nova.module.impl.OwnNametag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

/**
 * Hit Color: vanilla writes the hurt tint as put(1, 0, 0, 0.3) (red, 30%).
 * These four redirects swap in the chosen color, in that same order.
 */
@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {
    private static final String PUT = "Ljava/nio/FloatBuffer;put(F)Ljava/nio/FloatBuffer;";

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = PUT, ordinal = 0))
    private FloatBuffer nova$hitRed(FloatBuffer buffer, float value) {
        return buffer.put(hitColor(0, value));
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = PUT, ordinal = 1))
    private FloatBuffer nova$hitGreen(FloatBuffer buffer, float value) {
        return buffer.put(hitColor(1, value));
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = PUT, ordinal = 2))
    private FloatBuffer nova$hitBlue(FloatBuffer buffer, float value) {
        return buffer.put(hitColor(2, value));
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = PUT, ordinal = 3))
    private FloatBuffer nova$hitAlpha(FloatBuffer buffer, float value) {
        return buffer.put(hitColor(3, value));
    }

    /** Own Nametag: Minecraft never draws your own name; in third person, allow it. */
    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
    private void nova$ownNametag(EntityLivingBase entity, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView != 0 && NovaClient.isEnabled(OwnNametag.class)) {
            cir.setReturnValue(Minecraft.isGuiEnabled() && !entity.isInvisible());
        }
    }

    private static float hitColor(int channel, float vanilla) {
        return NovaClient.isEnabled(HitColor.class) ? NovaClient.get(HitColor.class).getColor()[channel] : vanilla;
    }
}
