package com.nova.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** 2D drawing helpers: rounded rectangles, circles, gradients, clipping and item icons. */
public final class Render {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private Render() {
    }

    public static void rect(float x, float y, float w, float h, int color) {
        if (w <= 0 || h <= 0) return;
        begin(color);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x, y + h, 0).endVertex();
        wr.pos(x + w, y + h, 0).endVertex();
        wr.pos(x + w, y, 0).endVertex();
        wr.pos(x, y, 0).endVertex();
        Tessellator.getInstance().draw();
        end();
    }

    public static void roundedRect(float x, float y, float w, float h, float radius, int color) {
        if (w <= 0 || h <= 0) return;
        radius = Math.min(radius, Math.min(w, h) / 2f);
        if (radius <= 0.5f) {
            rect(x, y, w, h, color);
            return;
        }
        begin(color);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        corner(wr, x + w - radius, y + radius, radius, 270);
        corner(wr, x + w - radius, y + h - radius, radius, 0);
        corner(wr, x + radius, y + h - radius, radius, 90);
        corner(wr, x + radius, y + radius, radius, 180);
        Tessellator.getInstance().draw();
        end();
    }

    private static void corner(WorldRenderer wr, float cx, float cy, float r, int startDeg) {
        for (int deg = startDeg; deg <= startDeg + 90; deg += 10) {
            double rad = Math.toRadians(deg);
            wr.pos(cx + Math.cos(rad) * r, cy + Math.sin(rad) * r, 0).endVertex();
        }
    }

    public static void circle(float cx, float cy, float r, int color) {
        begin(color);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(cx, cy, 0).endVertex();
        for (int deg = 360; deg >= 0; deg -= 10) {
            double rad = Math.toRadians(deg);
            wr.pos(cx + Math.cos(rad) * r, cy + Math.sin(rad) * r, 0).endVertex();
        }
        Tessellator.getInstance().draw();
        end();
    }

    /** A ring from startDeg sweeping clockwise by sweepDeg (used for cooldown circles). */
    public static void arc(float cx, float cy, float r, float thickness, float startDeg, float sweepDeg, int color) {
        if (sweepDeg <= 0) return;
        begin(color);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION);
        int steps = Math.max(2, (int) (sweepDeg / 6));
        for (int i = 0; i <= steps; i++) {
            double rad = Math.toRadians(startDeg - 90 + sweepDeg * i / steps);
            wr.pos(cx + Math.cos(rad) * r, cy + Math.sin(rad) * r, 0).endVertex();
            wr.pos(cx + Math.cos(rad) * (r - thickness), cy + Math.sin(rad) * (r - thickness), 0).endVertex();
        }
        Tessellator.getInstance().draw();
        end();
    }

    public static void outline(float x, float y, float w, float h, float t, int color) {
        rect(x - t, y - t, w + t * 2, t, color);
        rect(x - t, y + h, w + t * 2, t, color);
        rect(x - t, y, t, h, color);
        rect(x + w, y, t, h, color);
    }

    public static void verticalGradient(float x, float y, float w, float h, int top, int bottom) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        color(wr.pos(x + w, y, 0), top).endVertex();
        color(wr.pos(x, y, 0), top).endVertex();
        color(wr.pos(x, y + h, 0), bottom).endVertex();
        color(wr.pos(x + w, y + h, 0), bottom).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static WorldRenderer color(WorldRenderer wr, int argb) {
        return wr.color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, (argb >>> 24) & 255);
    }

    public static void item(ItemStack stack, float x, float y, float scale) {
        if (stack == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /** Only draw inside this rectangle (GUI coordinates) until {@link #endClip()}. */
    public static void clip(float x, float y, float w, float h) {
        ScaledResolution res = new ScaledResolution(mc);
        int f = res.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x * f), (int) (mc.displayHeight - (y + h) * f),
                (int) Math.max(0, w * f), (int) Math.max(0, h * f));
    }

    public static void endClip() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private static void begin(int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f,
                (color & 255) / 255f, ((color >>> 24) & 255) / 255f);
    }

    private static void end() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    // ---- color helpers ----

    public static int alpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 255) * Math.max(0f, Math.min(1f, alpha)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static int mix(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((from >>> 24) & 255) + (((to >>> 24) & 255) - ((from >>> 24) & 255)) * t);
        int r = (int) (((from >> 16) & 255) + (((to >> 16) & 255) - ((from >> 16) & 255)) * t);
        int g = (int) (((from >> 8) & 255) + (((to >> 8) & 255) - ((from >> 8) & 255)) * t);
        int b = (int) ((from & 255) + ((to & 255) - (from & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Rainbow color that moves over time; offset shifts it (for waves down a list). */
    public static int chroma(float offset) {
        float hue = ((System.currentTimeMillis() % 4000) / 4000f + offset) % 1f;
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.65f, 1f) & 0xFFFFFF);
    }
}
