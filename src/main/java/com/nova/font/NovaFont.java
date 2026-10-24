package com.nova.font;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Smooth anti-aliased text using a system font (Segoe UI on Windows).
 * Glyphs are drawn once into a texture at 2x size, then drawn at half size so they stay sharp.
 */
public final class NovaFont {
    private static final int ATLAS = 1024;
    private static final int PAD = 3;
    private static final float SCALE = 0.5f;
    private static final int[] COLOR_CODES = new int[16];

    static {
        for (int i = 0; i < 16; i++) {
            int base = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + base;
            int g = (i >> 1 & 1) * 170 + base;
            int b = (i & 1) * 170 + base;
            if (i == 6) r += 85;
            COLOR_CODES[i] = (r << 16) | (g << 8) | b;
        }
    }

    private final Font font;
    private final int[] glyphX = new int[256];
    private final int[] glyphY = new int[256];
    private final int[] glyphW = new int[256];
    private final int[] advance = new int[256];
    private int lineHeight;
    private int ascent;
    private int textureId = -1;

    public NovaFont(boolean bold, float size) {
        String family = pickFamily(bold);
        int style = bold && !family.endsWith("Semibold") ? Font.BOLD : Font.PLAIN;
        this.font = new Font(family, style, 1).deriveFont(style, size * 2f);
    }

    private static String pickFamily(boolean bold) {
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        if (bold && Arrays.asList(families).contains("Segoe UI Semibold")) return "Segoe UI Semibold";
        if (Arrays.asList(families).contains("Segoe UI")) return "Segoe UI";
        return Font.SANS_SERIF;
    }

    private void setup() {
        BufferedImage image = new BufferedImage(ATLAS, ATLAS, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics();
        lineHeight = metrics.getHeight();
        ascent = metrics.getAscent();

        int x = 0;
        int y = 0;
        for (int c = 32; c < 256; c++) {
            int adv = metrics.charWidth((char) c);
            int cell = adv + PAD * 2;
            if (x + cell >= ATLAS) {
                x = 0;
                y += lineHeight + 2;
            }
            g.drawString(String.valueOf((char) c), x + PAD, y + ascent);
            glyphX[c] = x;
            glyphY[c] = y;
            glyphW[c] = cell;
            advance[c] = adv;
            x += cell;
        }
        g.dispose();

        DynamicTexture texture = new DynamicTexture(image);
        textureId = texture.getGlTextureId();
        GlStateManager.bindTexture(textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    public float draw(String text, float x, float y, int color) {
        return draw(text, x, y, color, false);
    }

    public float drawWithShadow(String text, float x, float y, int color) {
        return draw(text, x, y, color, true);
    }

    public float drawCentered(String text, float centerX, float y, int color) {
        return draw(text, centerX - getWidth(text) / 2f, y, color, false);
    }

    public float draw(String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return x;
        if (textureId == -1) setup();
        if ((color & 0xFC000000) == 0) color |= 0xFF000000;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(SCALE, SCALE, 1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(textureId);
        GlStateManager.color(1f, 1f, 1f, 1f);

        if (shadow) {
            int shadowColor = ((color >>> 24) / 2 << 24) | 0x101010;
            pass(text, 2f, 2f, shadowColor, true);
        }
        float width = pass(text, 0f, 0f, color, false);

        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
        return x + width * SCALE;
    }

    private float pass(String text, float offX, float offY, int baseColor, boolean shadow) {
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        int alpha = (baseColor >>> 24) & 255;
        int color = baseColor;
        float cursor = 0f;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                int code = "0123456789abcdefr".indexOf(Character.toLowerCase(text.charAt(++i)));
                if (!shadow) {
                    if (code >= 0 && code < 16) color = (alpha << 24) | COLOR_CODES[code];
                    else if (code == 16) color = baseColor;
                }
                continue;
            }
            if (c >= 256) c = '?';
            if (c < 32) continue;

            float px = cursor - PAD + offX;
            float py = offY;
            float w = glyphW[c];
            float h = lineHeight;
            float u1 = glyphX[c] / (float) ATLAS;
            float v1 = glyphY[c] / (float) ATLAS;
            float u2 = (glyphX[c] + w) / ATLAS;
            float v2 = (glyphY[c] + h) / ATLAS;
            int r = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255, a = (color >>> 24) & 255;

            wr.pos(px, py, 0).tex(u1, v1).color(r, g, b, a).endVertex();
            wr.pos(px, py + h, 0).tex(u1, v2).color(r, g, b, a).endVertex();
            wr.pos(px + w, py + h, 0).tex(u2, v2).color(r, g, b, a).endVertex();
            wr.pos(px + w, py, 0).tex(u2, v1).color(r, g, b, a).endVertex();
            cursor += advance[c];
        }
        Tessellator.getInstance().draw();
        return cursor;
    }

    public float getWidth(String text) {
        if (text == null) return 0;
        if (textureId == -1) setup();
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                i++;
                continue;
            }
            if (c >= 256) c = '?';
            if (c >= 32) width += advance[c];
        }
        return width * SCALE;
    }

    public float getHeight() {
        if (textureId == -1) setup();
        return lineHeight * SCALE;
    }

    /** Y to draw at so the text is vertically centered in a box of the given height. */
    public float centerY(float boxY, float boxHeight) {
        return boxY + (boxHeight - getHeight()) / 2f;
    }
}
