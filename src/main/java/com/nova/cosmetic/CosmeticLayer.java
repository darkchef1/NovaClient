package com.nova.cosmetic;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.util.Map;

/** Added to Minecraft's player renderers: draws wings, head cosmetics and auras on players. */
public class CosmeticLayer implements LayerRenderer<AbstractClientPlayer> {
    private final CosmeticRenderer.Pose pose = new CosmeticRenderer.Pose();

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible()) return;
        Map<CosmeticSlot, String> set = Cosmetics.forPlayer(player);
        if (set == null || set.isEmpty()) return;

        pose.ticks = ageInTicks;
        pose.headYaw = netHeadYaw;
        pose.headPitch = headPitch;
        pose.sneaking = player.isSneaking();
        boolean airborne = !player.onGround || player.capabilities.isFlying;
        pose.effort = player.isSprinting() || airborne ? 1f : Math.min(1f, limbSwingAmount * 1.5f) * 0.5f;
        pose.light = player.getBrightnessForRender(partialTicks);
        CosmeticRenderer.renderExtras(set, pose);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
