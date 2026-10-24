package com.nova.mixin;

import com.nova.NovaClient;
import com.nova.module.impl.ItemPhysics;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Item Physics: func_177077_a positions a dropped item (bobbing and spinning); we replace it. */
@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem {
    @Inject(method = "func_177077_a", at = @At("HEAD"), cancellable = true)
    private void nova$itemPhysics(EntityItem item, double x, double y, double z, float partialTicks,
                                  IBakedModel model, CallbackInfoReturnable<Integer> cir) {
        if (NovaClient.isEnabled(ItemPhysics.class)) {
            cir.setReturnValue(ItemPhysics.transform(item, x, y, z, partialTicks, model));
        }
    }
}
