package com.nova.gui;

import com.nova.util.SkinLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;

/**
 * Draws your player model in 3D without needing a world: idle breathing, arms swaying,
 * and the head following the mouse. Used on the main menu.
 */
public final class PlayerPreview {
    private static final ModelPlayer WIDE = new ModelPlayer(0f, false);
    private static final ModelPlayer SLIM = new ModelPlayer(0f, true);

    private PlayerPreview() {
    }

    /**
     * @param x       where the feet stand (screen coordinates)
     * @param y       the bottom of the feet
     * @param size    pixels per block (the player is about 1.8 blocks tall)
     * @param yaw     how far the body is turned, in degrees (0 = facing you)
     * @param lookX   where the mouse is relative to the head, for the head to look at it
     * @param lookY   same, vertically
     */
    public static void draw(float x, float y, float size, float yaw, float lookX, float lookY) {
        Minecraft mc = Minecraft.getMinecraft();
        ModelPlayer model = SkinLoader.isSlim() ? SLIM : WIDE;
        pose(model, lookX, lookY);

        GlStateManager.enableDepth();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 100f);
        GlStateManager.scale(-size, size, size);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        // light it from the front-left like the inventory screen does
        GlStateManager.rotate(135f, 0f, 1f, 0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135f, 0f, 1f, 0f);

        GlStateManager.rotate(180f + yaw, 0f, 1f, 0f);
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.scale(0.9375f, 0.9375f, 0.9375f);
        GlStateManager.translate(0f, -1.5078125f, 0f);

        GlStateManager.disableCull();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(SkinLoader.getSkin());

        float s = 0.0625f;
        model.bipedHead.render(s);
        model.bipedBody.render(s);
        model.bipedRightArm.render(s);
        model.bipedLeftArm.render(s);
        model.bipedRightLeg.render(s);
        model.bipedLeftLeg.render(s);
        // the outer skin layer (hat, jacket, sleeves, pants)
        model.bipedHeadwear.render(s);
        model.bipedBodyWear.render(s);
        model.bipedRightArmwear.render(s);
        model.bipedLeftArmwear.render(s);
        model.bipedRightLegwear.render(s);
        model.bipedLeftLegwear.render(s);

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableDepth();
    }

    /** Idle animation: breathing arms, a gentle sway, and the head turned toward the mouse. */
    private static void pose(ModelPlayer m, float lookX, float lookY) {
        float t = (System.currentTimeMillis() % 100_000L) / 50f; // about 20 per second, like game ticks

        // turn the head toward the mouse, but only so far (like a real neck).
        // The model is drawn mirrored on screen, so turning toward the right is a negative angle.
        m.bipedHead.rotateAngleY = clamp(-(float) Math.atan(lookX / 80f) * 0.8f, 0.7f);
        m.bipedHead.rotateAngleX = clamp((float) Math.atan(lookY / 80f) * 0.6f, 0.45f);

        m.bipedRightArm.rotateAngleZ = (float) Math.cos(t * 0.09f) * 0.05f + 0.05f;
        m.bipedLeftArm.rotateAngleZ = -(float) Math.cos(t * 0.09f) * 0.05f - 0.05f;
        m.bipedRightArm.rotateAngleX = (float) Math.sin(t * 0.067f) * 0.05f;
        m.bipedLeftArm.rotateAngleX = -(float) Math.sin(t * 0.067f) * 0.05f;
        m.bipedBody.rotateAngleY = 0f;
        m.bipedRightLeg.rotateAngleX = 0f;
        m.bipedLeftLeg.rotateAngleX = 0f;

        ModelBase.copyModelAngles(m.bipedHead, m.bipedHeadwear);
        ModelBase.copyModelAngles(m.bipedBody, m.bipedBodyWear);
        ModelBase.copyModelAngles(m.bipedRightArm, m.bipedRightArmwear);
        ModelBase.copyModelAngles(m.bipedLeftArm, m.bipedLeftArmwear);
        ModelBase.copyModelAngles(m.bipedRightLeg, m.bipedRightLegwear);
        ModelBase.copyModelAngles(m.bipedLeftLeg, m.bipedLeftLegwear);
    }

    private static float clamp(float value, float limit) {
        return Math.max(-limit, Math.min(limit, value));
    }
}
