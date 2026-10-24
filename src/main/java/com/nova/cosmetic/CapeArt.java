package com.nova.cosmetic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Draws the Nova capes in code and turns them into textures.
 * <p>
 * A Minecraft cape texture is 64x32 pixels; the cape's outside is the 10x16 area at (1, 1) and
 * its inside the one at (12, 1). Nova draws at 4x that size so capes look sharp.
 */
public final class CapeArt {
    private static final int S = 4;            // texture pixels per cape pixel
    private static final int FACE_W = 10 * S;
    private static final int FACE_H = 16 * S;
    private static final int FRAMES = 24;
    private static final long FRAME_MS = 90;

    private static final Map<String, ResourceLocation[]> textures = new HashMap<>();

    private CapeArt() {
    }

    /** The cape's texture right now (animated capes return the current frame). */
    public static ResourceLocation texture(String id) {
        ResourceLocation[] frames = textures.get(id);
        if (frames == null) {
            frames = build(id);
            textures.put(id, frames);
        }
        if (frames.length == 1) return frames[0];
        return frames[(int) ((System.currentTimeMillis() / FRAME_MS) % frames.length)];
    }

    private static boolean isAnimated(String id) {
        switch (id) {
            case "galaxy": case "aurora": case "rainbow": case "lava": case "flame":
            case "matrix": case "neon": case "storm": case "void":
                return true;
            default:
                return false;
        }
    }

    private static ResourceLocation[] build(String id) {
        int count = isAnimated(id) ? FRAMES : 1;
        ResourceLocation[] frames = new ResourceLocation[count];
        BufferedImage custom = id.startsWith(Cosmetics.CUSTOM_PREFIX) ? loadCustom(id) : null;
        for (int i = 0; i < count; i++) {
            BufferedImage face = new BufferedImage(FACE_W, FACE_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = face.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            paint(g, id, i / (float) count, custom);
            g.dispose();
            DynamicTexture texture = new DynamicTexture(layout(face));
            frames[i] = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("nova_cape_" + id.replaceAll("[^a-z0-9_]", "_") + "_" + i, texture);
        }
        return frames;
    }

    /** Places the design on the outside and inside of the cape, and edges around them. */
    private static BufferedImage layout(BufferedImage face) {
        BufferedImage tex = new BufferedImage(64 * S, 32 * S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tex.createGraphics();
        Color edge = darker(new Color(face.getRGB(FACE_W / 2, FACE_H - 2), true), 0.6f);
        g.setColor(edge);
        g.fillRect(0, S, S, 16 * S);           // left edge
        g.fillRect(11 * S, S, S, 16 * S);      // right edge
        g.fillRect(S, 0, 10 * S, S);           // top
        g.fillRect(11 * S, 0, 10 * S, S);      // bottom
        g.drawImage(face, S, S, null);         // outside
        g.drawImage(face, 12 * S, S, null);    // inside
        g.dispose();
        return tex;
    }

    // ------------------------------------------------------------------ designs

    private static void paint(Graphics2D g, String id, float t, BufferedImage custom) {
        int w = FACE_W, h = FACE_H;
        if (custom != null) {
            // cover the cape with the picture, cropping whatever doesn't fit
            float scale = Math.max(w / (float) custom.getWidth(), h / (float) custom.getHeight());
            int dw = Math.round(custom.getWidth() * scale), dh = Math.round(custom.getHeight() * scale);
            g.drawImage(custom, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            return;
        }
        switch (id) {
            case "galaxy": galaxy(g, w, h, t); break;
            case "aurora": aurora(g, w, h, t); break;
            case "rainbow": rainbow(g, w, h, t); break;
            case "sunset": sunset(g, w, h); break;
            case "ice": ice(g, w, h); break;
            case "crimson": crimson(g, w, h); break;
            case "midnight": midnight(g, w, h); break;
            case "lava": lava(g, w, h, t); break;
            case "flame": flame(g, w, h, t); break;
            case "matrix": matrix(g, w, h, t); break;
            case "neon": neon(g, w, h, t); break;
            case "storm": storm(g, w, h, t); break;
            case "void": voidCape(g, w, h, t); break;
            case "ocean": ocean(g, w, h); break;
            case "forest": forest(g, w, h); break;
            case "sakura": sakura(g, w, h); break;
            case "gold": gold(g, w, h); break;
            case "checker": checker(g, w, h); break;
            case "candy": candy(g, w, h); break;
            default: nova(g, w, h);
        }
    }

    // ---- animated

    /** Glowing orange cracks drifting slowly over dark rock. */
    private static void lava(Graphics2D g, int w, int h, float t) {
        g.setColor(new Color(0x1E0A06));
        g.fillRect(0, 0, w, h);
        double a = t * Math.PI * 2;
        for (int i = 0; i < 4; i++) {
            float cx = w * (0.2f + 0.2f * i) + (float) Math.sin(a + i) * 4;
            float cy = h * ((t + i * 0.27f) % 1f);
            glow(g, cx, cy, w * 0.55f, new Color(255, 90, 20), 0.55f);
        }
        g.setStroke(new BasicStroke(2.2f));
        Random random = new Random(21);
        for (int i = 0; i < 9; i++) {
            float x = random.nextFloat() * w;
            float y = (random.nextFloat() * h + t * h) % h;
            GeneralPath crack = new GeneralPath();
            crack.moveTo(x, y);
            for (int s = 0; s < 4; s++) crack.lineTo(x + (random.nextFloat() - 0.5f) * 14, y + s * 6 + random.nextFloat() * 4);
            g.setColor(new Color(255, 170 + random.nextInt(60), 40, 220));
            g.draw(crack);
        }
    }

    /** Flames licking up from the bottom of the cape. */
    private static void flame(Graphics2D g, int w, int h, float t) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x140404), 0, h, new Color(0x3A0A00)));
        g.fillRect(0, 0, w, h);
        Color[] colors = {new Color(255, 60, 0, 200), new Color(255, 140, 0, 200), new Color(255, 220, 80, 210)};
        for (int layer = 0; layer < 3; layer++) {
            GeneralPath fire = new GeneralPath();
            fire.moveTo(0, h);
            float base = h * (0.35f + layer * 0.18f);
            for (int x = 0; x <= w; x += 2) {
                double phase = t * Math.PI * 2 * (layer + 1);
                float y = base + (float) (Math.sin(x * 0.35 + phase) * 5 + Math.sin(x * 0.13 - phase * 0.7) * 7);
                fire.lineTo(x, y);
            }
            fire.lineTo(w, h);
            fire.closePath();
            g.setColor(colors[layer]);
            g.fill(fire);
        }
    }

    /** Falling green characters, like in The Matrix. */
    private static void matrix(Graphics2D g, int w, int h, float t) {
        g.setColor(new Color(0x020A04));
        g.fillRect(0, 0, w, h);
        int cell = 4;
        for (int col = 0; col < w / cell; col++) {
            Random random = new Random(col * 31L + 5); // each column keeps its own speed and pattern every frame
            float speed = 0.6f + random.nextFloat();
            float head = ((t * speed + random.nextFloat()) % 1f) * (h + 30) - 15;
            for (int row = 0; row < h / cell; row++) {
                float dist = head - row * cell;
                if (dist < 0 || dist > 30) continue;
                float alpha = 1f - dist / 30f;
                boolean lit = random.nextFloat() < 0.75f;
                if (!lit) continue;
                g.setColor(dist < 4 ? new Color(200, 255, 200, (int) (255 * alpha)) : new Color(40, 255, 90, (int) (220 * alpha)));
                g.fillRect(col * cell, row * cell, cell - 1, cell - 1);
            }
        }
    }

    /** A dark cape with a glowing outline that cycles through colors. */
    private static void neon(Graphics2D g, int w, int h, float t) {
        g.setColor(new Color(0x08060E));
        g.fillRect(0, 0, w, h);
        Color c = Color.getHSBColor(t, 0.85f, 1f);
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60 - i * 12));
            g.setStroke(new BasicStroke(6f - i));
            g.drawRect(4 + i, 4 + i, w - 9 - i * 2, h - 9 - i * 2);
        }
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(5, 5, w - 11, h - 11);
        star(g, w / 2f, h * 0.45f, w * 0.22f, w * 0.05f, t * 90f, c);
    }

    /** Dark clouds with a lightning bolt that flashes now and then. */
    private static void storm(Graphics2D g, int w, int h, float t) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x1B2130), 0, h, new Color(0x0A0D14)));
        g.fillRect(0, 0, w, h);
        Random random = new Random(9);
        for (int i = 0; i < 7; i++) {
            float x = random.nextFloat() * w, y = random.nextFloat() * h * 0.4f;
            g.setColor(new Color(70, 80, 100, 120));
            g.fillOval((int) (x - 12 + Math.sin(t * Math.PI * 2 + i) * 3), (int) y, 26, 12);
        }
        boolean flash = t > 0.62f && t < 0.75f;
        if (flash) {
            glow(g, w / 2f, h * 0.4f, w, new Color(160, 190, 255), 0.5f);
            GeneralPath bolt = new GeneralPath();
            bolt.moveTo(w * 0.55f, h * 0.15f);
            bolt.lineTo(w * 0.38f, h * 0.48f);
            bolt.lineTo(w * 0.52f, h * 0.48f);
            bolt.lineTo(w * 0.4f, h * 0.85f);
            bolt.lineTo(w * 0.66f, h * 0.4f);
            bolt.lineTo(w * 0.52f, h * 0.4f);
            bolt.closePath();
            g.setColor(new Color(255, 255, 230));
            g.fill(bolt);
        }
    }

    /** A black hole: a swirling ring of light around a dark center. */
    private static void voidCape(Graphics2D g, int w, int h, float t) {
        g.setColor(new Color(0x050008));
        g.fillRect(0, 0, w, h);
        stars(g, w, h, 30, 13, t);
        float cx = w / 2f, cy = h * 0.45f;
        for (int i = 0; i < 40; i++) {
            double angle = i / 40.0 * Math.PI * 2 + t * Math.PI * 2;
            float r = w * 0.32f + (float) Math.sin(i * 1.7) * 3;
            float x = cx + (float) Math.cos(angle) * r, y = cy + (float) Math.sin(angle) * r * 0.45f;
            g.setColor(new Color(180, 90, 255, 150));
            g.fillOval((int) x - 2, (int) y - 2, 4, 4);
        }
        glow(g, cx, cy, w * 0.5f, new Color(140, 60, 255), 0.5f);
        g.setColor(Color.BLACK);
        g.fillOval((int) (cx - w * 0.16f), (int) (cy - w * 0.16f), (int) (w * 0.32f), (int) (w * 0.32f));
    }

    // ---- still

    private static void ocean(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x7FD8FF), 0, h, new Color(0x05325E)));
        g.fillRect(0, 0, w, h);
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 7; i++) {
            float y = h * (0.3f + i * 0.1f);
            GeneralPath wave = new GeneralPath();
            wave.moveTo(0, y);
            for (int x = 0; x <= w; x += 2) wave.lineTo(x, y + (float) Math.sin(x * 0.3 + i) * 2.5f);
            g.setColor(new Color(255, 255, 255, 90 - i * 10));
            g.draw(wave);
        }
    }

    private static void forest(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xB8E0C8), 0, h, new Color(0x1E4A2E)));
        g.fillRect(0, 0, w, h);
        Random random = new Random(3);
        for (int layer = 0; layer < 2; layer++) {
            g.setColor(layer == 0 ? new Color(0x2F6B45) : new Color(0x143322));
            for (int i = 0; i < 6; i++) {
                int cx = random.nextInt(w), base = (int) (h * (0.62f + layer * 0.18f)), size = 10 + random.nextInt(8);
                g.fillPolygon(new int[]{cx, cx + size / 2, cx - size / 2}, new int[]{base - size * 2, base, base}, 3);
            }
            g.fillRect(0, (int) (h * (0.62f + layer * 0.18f)), w, h);
        }
    }

    private static void sakura(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xFFE4EF), 0, h, new Color(0xF7A8C8)));
        g.fillRect(0, 0, w, h);
        Random random = new Random(11);
        for (int i = 0; i < 26; i++) {
            int x = random.nextInt(w), y = random.nextInt(h), s = 3 + random.nextInt(4);
            g.setColor(new Color(255, 120 + random.nextInt(60), 170, 210));
            g.fillOval(x, y, s, s - 1);
        }
        g.setColor(new Color(0x6B3A2A));
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(0, (int) (h * 0.2f), (int) (w * 0.7f), (int) (h * 0.35f));
        g.drawLine((int) (w * 0.4f), (int) (h * 0.29f), (int) (w * 0.55f), (int) (h * 0.15f));
    }

    private static void gold(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xFFE38A), w, h, new Color(0xB8860B)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 70));
        g.setStroke(new BasicStroke(4f));
        g.drawLine(-5, h / 3, w + 5, h / 5);
        g.setColor(new Color(120, 80, 0, 140));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(3, 3, w - 7, h - 7);
        star(g, w / 2f, h * 0.45f, w * 0.25f, w * 0.06f, 0, new Color(255, 250, 220));
    }

    private static void checker(Graphics2D g, int w, int h) {
        int cell = 8;
        for (int x = 0; x < w; x += cell) {
            for (int y = 0; y < h; y += cell) {
                g.setColor(((x + y) / cell) % 2 == 0 ? new Color(0x111111) : new Color(0xF2F2F2));
                g.fillRect(x, y, cell, cell);
            }
        }
    }

    private static void candy(Graphics2D g, int w, int h) {
        g.setColor(new Color(0xFFF4F8));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0xFF4F7B));
        g.setStroke(new BasicStroke(6f));
        for (int i = -h; i < w + h; i += 14) g.drawLine(i, 0, i + h, h);
    }

    private static void nova(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x161B3D), 0, h, new Color(0x2A1450)));
        g.fillRect(0, 0, w, h);
        glow(g, w / 2f, h * 0.42f, w * 0.75f, new Color(76, 157, 255), 0.55f);
        stars(g, w, h, 18, 7, 0f);
        star(g, w / 2f, h * 0.42f, w * 0.36f, w * 0.07f, 0, Color.WHITE);
        star(g, w / 2f, h * 0.42f, w * 0.2f, w * 0.05f, 45, new Color(76, 157, 255));
        g.setColor(Color.WHITE);
        g.fillOval(w / 2 - 2, (int) (h * 0.42f) - 2, 4, 4);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(0, h - S, w, S);
    }

    private static void galaxy(Graphics2D g, int w, int h, float t) {
        g.setColor(new Color(0x0B0820));
        g.fillRect(0, 0, w, h);
        double a = t * Math.PI * 2;
        glow(g, w * (0.35f + 0.08f * (float) Math.cos(a)), h * (0.3f + 0.05f * (float) Math.sin(a)), w * 0.9f, new Color(140, 60, 255), 0.6f);
        glow(g, w * (0.7f + 0.07f * (float) Math.sin(a)), h * (0.62f + 0.05f * (float) Math.cos(a)), w * 0.85f, new Color(40, 140, 255), 0.55f);
        glow(g, w * 0.4f, h * (0.85f + 0.04f * (float) Math.sin(a * 2)), w * 0.6f, new Color(255, 80, 180), 0.4f);
        stars(g, w, h, 45, 11, t);
    }

    private static void aurora(Graphics2D g, int w, int h, float t) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x041019), 0, h, new Color(0x0A2238)));
        g.fillRect(0, 0, w, h);
        stars(g, w, h, 25, 3, t);
        double a = t * Math.PI * 2;
        Color[] colors = {new Color(60, 255, 170), new Color(40, 210, 255), new Color(170, 90, 255)};
        for (int band = 0; band < 3; band++) {
            GeneralPath path = new GeneralPath();
            float base = h * (0.28f + band * 0.18f);
            path.moveTo(0, h);
            for (int x = 0; x <= w; x += 2) {
                float y = base + (float) (Math.sin(x * 0.12 + a + band * 1.7) * h * 0.06 + Math.sin(x * 0.05 - a * 2 + band) * h * 0.04);
                path.lineTo(x, y);
            }
            path.lineTo(w, h);
            path.closePath();
            Color c = colors[band];
            g.setPaint(new GradientPaint(0, base - h * 0.1f, new Color(c.getRed(), c.getGreen(), c.getBlue(), 150),
                    0, base + h * 0.35f, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)));
            g.fill(path);
        }
    }

    private static void rainbow(Graphics2D g, int w, int h, float t) {
        for (int y = 0; y < h; y++) {
            float hue = (y / (float) h * 0.9f + t) % 1f;
            g.setColor(Color.getHSBColor(hue, 0.65f, 1f));
            g.fillRect(0, y, w, 1);
        }
        g.setColor(new Color(255, 255, 255, 70));
        star(g, w / 2f, h * 0.45f, w * 0.3f, w * 0.06f, 0, new Color(255, 255, 255, 200));
    }

    private static void sunset(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xFFB347), 0, h * 0.7f, new Color(0xFF5E62)));
        g.fillRect(0, 0, w, h);
        // a retro sun cut by stripes, setting behind the horizon
        g.setColor(new Color(255, 236, 160));
        int r = (int) (w * 0.32f);
        g.fillOval(w / 2 - r, (int) (h * 0.55f) - r, r * 2, r * 2);
        g.setColor(new Color(0xFF7A5A));
        for (int i = 0; i < 4; i++) g.fillRect(0, (int) (h * 0.4f) + i * 6 + i * i, w, 1 + i);
        g.setPaint(new GradientPaint(0, h * 0.55f, new Color(0x7A2E6E), 0, h, new Color(0x2A1040)));
        g.fillRect(0, (int) (h * 0.55f), w, h);
    }

    private static void ice(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xE6F8FF), 0, h, new Color(0x6CC4F5)));
        g.fillRect(0, 0, w, h);
        Random random = new Random(7);
        for (int i = 0; i < 9; i++) {
            int cx = random.nextInt(w), cy = random.nextInt(h), size = 6 + random.nextInt(12);
            Polygon shard = new Polygon(new int[]{cx, cx + size / 3, cx, cx - size / 3}, new int[]{cy - size, cy, cy + size, cy}, 4);
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(shard);
            g.setColor(new Color(120, 190, 240, 180));
            g.draw(shard);
        }
        star(g, w / 2f, h * 0.4f, w * 0.28f, w * 0.05f, 0, new Color(255, 255, 255, 230));
    }

    private static void crimson(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x8B0015), 0, h, new Color(0x2A0006)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0, 0, 0, 60));
        g.setStroke(new BasicStroke(3f));
        for (int i = -h; i < w + h; i += 9) g.drawLine(i, 0, i + h, h);
        Polygon diamond = new Polygon(new int[]{w / 2, w / 2 + 8, w / 2, w / 2 - 8}, new int[]{(int) (h * 0.4f) - 12, (int) (h * 0.4f), (int) (h * 0.4f) + 12, (int) (h * 0.4f)}, 4);
        g.setColor(new Color(0xF2C230));
        g.fill(diamond);
        g.setColor(new Color(0x8A6A10));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(diamond);
    }

    private static void midnight(Graphics2D g, int w, int h) {
        g.setColor(new Color(0x0A0A10));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(76, 157, 255));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(3, 3, w - 7, h - 7);
        g.setColor(new Color(76, 157, 255, 90));
        g.drawRect(7, 7, w - 15, h - 15);
        star(g, w / 2f, h / 2f, w * 0.18f, w * 0.04f, 0, new Color(76, 157, 255));
    }

    // ------------------------------------------------------------------ drawing helpers

    private static void glow(Graphics2D g, float cx, float cy, float radius, Color color, float strength) {
        Color center = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(255 * strength));
        Color edge = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        g.setPaint(new RadialGradientPaint(cx, cy, radius, new float[]{0f, 1f}, new Color[]{center, edge}));
        g.fillRect(0, 0, FACE_W, FACE_H);
    }

    /** Little stars; t makes them twinkle (0 to 1 over the animation). */
    private static void stars(Graphics2D g, int w, int h, int count, long seed, float t) {
        Random random = new Random(seed);
        for (int i = 0; i < count; i++) {
            float x = random.nextFloat() * w, y = random.nextFloat() * h;
            float phase = random.nextFloat();
            float twinkle = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin((t + phase) * Math.PI * 2));
            int size = random.nextFloat() < 0.15f ? 2 : 1;
            g.setColor(new Color(1f, 1f, 1f, twinkle));
            g.fillRect(Math.round(x), Math.round(y), size, size);
        }
    }

    /** A four-point star like the Nova logo. */
    private static void star(Graphics2D g, float cx, float cy, float outer, float inner, float rotationDeg, Color color) {
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < 8; i++) {
            double rad = Math.toRadians(rotationDeg + i * 45 - 90);
            float r = i % 2 == 0 ? outer : inner;
            float x = cx + (float) Math.cos(rad) * r, y = cy + (float) Math.sin(rad) * r;
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(color);
        g.fill(path);
    }

    private static Color darker(Color c, float f) {
        return new Color(Math.round(c.getRed() * f), Math.round(c.getGreen() * f), Math.round(c.getBlue() * f));
    }

    private static BufferedImage loadCustom(String id) {
        try {
            return ImageIO.read(new File(Cosmetics.getCapesFolder(), id.substring(Cosmetics.CUSTOM_PREFIX.length())));
        } catch (Exception e) {
            return null;
        }
    }

    /** Forgets drawn capes so changed custom images are re-read. */
    public static void clearCustom() {
        textures.keySet().removeIf(id -> id.startsWith(Cosmetics.CUSTOM_PREFIX));
    }
}
