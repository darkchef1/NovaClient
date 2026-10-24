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
        // Go around counter-clockwise on screen, like Minecraft's own rectangles. Going the other way
        // makes the shape "face away", and it disappears whenever back faces are being skipped.
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        cornerReversed(wr, x + radius, y + radius, radius, 180);
        cornerReversed(wr, x + radius, y + h - radius, radius, 90);
        cornerReversed(wr, x + w - radius, y + h - radius, radius, 0);
        cornerReversed(wr, x + w - radius, y + radius, radius, 270);
        Tessellator.getInstance().draw();
        end();
    }

    /** One rounded corner, walked from startDeg + 90 back down to startDeg. */
    private static void cornerReversed(WorldRenderer wr, float cx, float cy, float r, int startDeg) {
        for (int deg = startDeg + 90; deg >= startDeg; deg -= 10) {
            double rad = Math.toRadians(deg);
            wr.pos(cx + Math.cos(rad) * r, cy + Math.sin(rad) * r, 0).endVertex();
        }
    }

    /** A thin border with rounded corners, matching {@link #roundedRect}. Width is in GUI pixels. */
    public static void roundedOutline(float x, float y, float w, float h, float radius, float width, int color) {
        if (w <= 0 || h <= 0) return;
        radius = Math.min(radius, Math.min(w, h) / 2f);
        int scale = new ScaledResolution(mc).getScaleFactor();
        begin(color);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(Math.max(1f, width * scale));
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        corner(wr, x + w - radius, y + radius, radius, 270);
        corner(wr, x + w - radius, y + h - radius, radius, 0);
        corner(wr, x + radius, y + h - radius, radius, 90);
        corner(wr, x + radius, y + radius, radius, 180);
        Tessellator.getInstance().draw();
        GL11.glLineWidth(1f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
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

    /** A star with the given number of points, rotated by rotationDeg. Used for the Nova logo. */
    public static void star(float cx, float cy, float outer, float inner, int points, float rotationDeg, int color) {
        begin(color);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(cx, cy, 0).endVertex();
        for (int i = points * 2; i >= 0; i--) {
            double rad = Math.toRadians(rotationDeg + i * 180.0 / points - 90);
            float r = i % 2 == 0 ? outer : inner;
            wr.pos(cx + Math.cos(rad) * r, cy + Math.sin(rad) * r, 0).endVertex();
        }
        Tessellator.getInstance().draw();
        end();
    }

    /** A soft glow: a circle that fades smoothly from the color in the middle to nothing at the edge. */
    public static void glow(float cx, float cy, float radius, int color, float strength) {
        int center = alpha(color, Math.min(1f, strength * 0.4f));
        int edge = color & 0x00FFFFFF;
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        color(wr.pos(cx, cy, 0), center).endVertex();
        for (int deg = 360; deg >= 0; deg -= 6) {
            double rad = Math.toRadians(deg);
            color(wr.pos(cx + Math.cos(rad) * radius, cy + Math.sin(rad) * radius, 0), edge).endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
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

    /** Draws part of a texture (u/v from 0 to 1) into a rectangle. */
    public static void texture(net.minecraft.util.ResourceLocation location, float x, float y, float w, float h,
                               float u1, float v1, float u2, float v2) {
        mc.getTextureManager().bindTexture(location);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.color(1f, 1f, 1f, 1f);
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0).tex(u1, v2).endVertex();
        wr.pos(x + w, y + h, 0).tex(u2, v2).endVertex();
        wr.pos(x + w, y, 0).tex(u2, v1).endVertex();
        wr.pos(x, y, 0).tex(u1, v1).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
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
        // Minecraft's GUIs throw away pixels under 10% opacity; turn that off so soft glows and faint fills show
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f,
                (color & 255) / 255f, ((color >>> 24) & 255) / 255f);
    }

    private static void end() {
        GlStateManager.enableAlpha();
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
