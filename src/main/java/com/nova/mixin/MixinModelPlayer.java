package com.nova.mixin;

import com.nova.emote.EmotePlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Emotes: after Minecraft poses the player's arms, legs and head, an emote can change them. */
@Mixin(ModelPlayer.class)
public abstract class MixinModelPlayer {
    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void nova$emote(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                            float headPitch, float scale, Entity entity, CallbackInfo ci) {
        EmotePlayer.apply((ModelPlayer) (Object) this, entity);
    }
}
