package com.nova.mixin;

import com.nova.NovaClient;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keep inventory centered: with potion effects active, vanilla pushes the inventory to the right
 * to make room for the effect list. This puts it back in the middle.
 */
@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends GuiContainer {
    public MixinInventoryEffectRenderer(Container container) {
        super(container);
    }

    @Inject(method = "updateActivePotionEffects", at = @At("TAIL"))
    private void nova$keepCentered(CallbackInfo ci) {
        if (NovaClient.getSettings() != null && NovaClient.getSettings().centeredInventory.isOn()) {
            guiLeft = (width - xSize) / 2;
        }
    }
}
