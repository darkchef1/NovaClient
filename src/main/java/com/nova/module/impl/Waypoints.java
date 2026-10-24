package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.util.ServerUtil;
import com.nova.waypoint.Waypoint;
import com.nova.waypoint.WaypointManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Draws your waypoints in the world: a label with name and distance, and an optional beam.
 * Add and manage them in the menu's WAYPOINTS tab. Hidden on Hypixel.
 */
public class Waypoints extends Module {
    /** Labels further than this are drawn at this distance instead, so they stay readable. */
    private static final double LABEL_DISTANCE = 12;

    private final BooleanSetting beam = add(new BooleanSetting("Show beam", true));
    private final BooleanSetting distance = add(new BooleanSetting("Show distance", true));

    public Waypoints() {
        super("Waypoints", "Markers in the world. Manage them in the WAYPOINTS tab", Category.MISC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.beacon);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled() || mc.theWorld == null || ServerUtil.isHypixel()) return;
        Entity view = mc.getRenderViewEntity();
        if (view == null) return;
        float pt = event.partialTicks;
        double camX = view.lastTickPosX + (view.posX - view.lastTickPosX) * pt;
        double camY = view.lastTickPosY + (view.posY - view.lastTickPosY) * pt;
        double camZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * pt;
        float eye = view.getEyeHeight();

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (Waypoint w : WaypointManager.forCurrentWorld()) {
            if (!w.visible) continue;
            double dx = w.x + 0.5 - camX;
            double dy = w.y - camY;
            double dz = w.z + 0.5 - camZ;
            if (beam.isOn()) drawBeam(dx, -camY, dz, w.getColor());
            drawLabel(w, dx, dy + 1.5 - eye, dz, eye);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void drawBeam(double x, double bottomY, double z, int color) {
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(3f);
        GlStateManager.color(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, 0.45f);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        wr.pos(x, bottomY, z).endVertex();
        wr.pos(x, bottomY + 256, z).endVertex();
        Tessellator.getInstance().draw();
        GL11.glLineWidth(1f);
        GlStateManager.enableTexture2D();
    }

    /** Draws the label facing you. (dx, dy, dz) is the waypoint relative to your eyes. */
    private void drawLabel(Waypoint w, double dx, double dy, double dz, float eye) {
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double shown = Math.min(dist, LABEL_DISTANCE);
        double f = dist > 0 ? shown / dist : 0;
        float scale = 0.0045f * (float) Math.max(shown, 1.5);

        RenderManager rm = mc.getRenderManager();
        GlStateManager.pushMatrix();
        GlStateManager.translate(dx * f, dy * f + eye, dz * f);
        GlStateManager.rotate(-rm.playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(rm.playerViewX * (mc.gameSettings.thirdPersonView == 2 ? -1f : 1f), 1f, 0f, 0f);
        GlStateManager.scale(-scale, -scale, scale);

        FontRenderer font = mc.fontRendererObj;
        String text = w.name + (distance.isOn() ? " §7[" + Math.round(dist) + "m]" : "");
        int half = font.getStringWidth(text) / 2;
        Gui.drawRect(-half - 3, -2, half + 3, 10, 0x90000000);
        Gui.drawRect(-half - 3, 10, half + 3, 11, w.getColor());
        GlStateManager.enableTexture2D();
        font.drawString(text, -half, 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }
}
