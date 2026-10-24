package com.nova.cosmetic;

import com.nova.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Map;

/**
 * Draws wings, head cosmetics and auras as 3D shapes on a player.
 * <p>
 * Everything is drawn in the player model's own space: 1 unit = 1 block, y points down,
 * the origin is the neck, and the back of the player faces +z.
 */
public final class CosmeticRenderer {
    private CosmeticRenderer() {
    }

    /** How the player is posed this frame. */
    public static final class Pose {
        /** Time in ticks (20 per second), for animation. */
        public float ticks;
        public float headYaw;
        public float headPitch;
        public boolean sneaking;
        /** 0 = standing still, 1 = sprinting or in the air: wings flap harder. */
        public float effort;
        /** The entity's light level to restore afterwards, or -1 in menus. */
        public int light = -1;
    }

    /** Draws everything except the cape (which Minecraft draws itself). */
    public static void renderExtras(Map<CosmeticSlot, String> set, Pose pose) {
        String wings = set.get(CosmeticSlot.WINGS);
        String head = set.get(CosmeticSlot.HEAD);
        String aura = set.get(CosmeticSlot.AURA);
        if (wings == null && head == null && aura == null) return;

        begin();
        if (wings != null) {
            GlStateManager.pushMatrix();
            if (pose.sneaking) GlStateManager.rotate(28.6f, 1f, 0f, 0f);
            wings(wings, pose);
            GlStateManager.popMatrix();
        }
        if (head != null) {
            GlStateManager.pushMatrix();
            if (pose.sneaking) GlStateManager.translate(0f, 0.2f, 0f);
            if (head.equals("crown")) crown(pose);
            else halo(pose);
            GlStateManager.popMatrix();
        }
        if (aura != null) aura(aura, pose);
        end(pose.light);
    }

    /** Draws a cape on a model with no entity behind it (the menu previews). */
    public static void renderPreviewCape(ModelPlayer model, String capeId) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(CapeArt.texture(capeId));
        float sway = (float) Math.sin(System.currentTimeMillis() / 700.0) * 2f;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 0f, 0.125f);
        GlStateManager.rotate(6f + sway, 1f, 0f, 0f);
        GlStateManager.rotate(180f, 0f, 1f, 0f);
        model.renderCape(0.0625f);
        GlStateManager.popMatrix();
    }

    // ------------------------------------------------------------------ GL state

    private static void begin() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        // cosmetics glow a little: draw them at full brightness even in the dark
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
    }

    private static void end(int light) {
        if (light >= 0) OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void additive(boolean on) {
        if (on) GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
        else GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    private static WorldRenderer start(int mode) {
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(mode, DefaultVertexFormats.POSITION_COLOR);
        return wr;
    }

    private static void v(WorldRenderer wr, double x, double y, double z, int argb) {
        wr.pos(x, y, z).color((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, (argb >>> 24) & 255).endVertex();
    }

    private static void draw() {
        Tessellator.getInstance().draw();
    }

    private static int argb(int rgb, float alpha) {
        return (Math.round(Math.max(0, Math.min(1, alpha)) * 255) << 24) | (rgb & 0xFFFFFF);
    }

    // ------------------------------------------------------------------ wings

    /** Bone tips of one wing in its own flat plane: x = outward, y = down (blocks). */
    private static final float[][] BONES = {{0.42f, -0.55f}, {0.78f, -0.32f}, {0.86f, 0.05f}, {0.68f, 0.42f}, {0.36f, 0.62f}};

    private static void wings(String style, Pose pose) {
        float speed = 0.1f + pose.effort * 0.22f;
        float swing = (float) Math.sin(pose.ticks * speed) * (10f + pose.effort * 22f);
        for (int side = -1; side <= 1; side += 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * 0.07f, 0.16f, 0.14f);      // shoulder blades
            GlStateManager.rotate(side * (-24f - swing), 0f, 1f, 0f);  // fold back and flap
            GlStateManager.rotate(side * -8f, 0f, 0f, 1f);             // tilt up a little
            switch (style) {
                case "dragon": dragonWing(side); break;
                case "crystal": crystalWing(side); break;
                default: angelWing(side, pose.ticks);
            }
            GlStateManager.popMatrix();
        }
    }

    /** The wing's outline: root, then bone tips with scalloped dips between them. */
    private static float[][] outline(float dip) {
        float[][] points = new float[BONES.length * 2][];
        int n = 0;
        for (int i = 0; i < BONES.length; i++) {
            points[n++] = BONES[i];
            if (i + 1 < BONES.length) {
                float[] a = BONES[i], b = BONES[i + 1];
                points[n++] = new float[]{(a[0] + b[0]) * 0.5f * dip, (a[1] + b[1]) * 0.5f * dip};
            }
        }
        float[][] trimmed = new float[n][];
        System.arraycopy(points, 0, trimmed, 0, n);
        return trimmed;
    }

    private static void membrane(int side, float[][] outline, int rootColor, int tipColor, float z) {
        WorldRenderer wr = start(GL11.GL_TRIANGLE_FAN);
        v(wr, 0, 0, z, rootColor);
        for (float[] p : outline) v(wr, side * p[0], p[1], z, tipColor);
        draw();
    }

    /** A thin strip from a to b, used for bones, feathers and glowing edges. */
    private static void strip(int side, float ax, float ay, float bx, float by, float width, int colorA, int colorB, float z) {
        float dx = bx - ax, dy = by - ay;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        float nx = -dy / len * width / 2f, ny = dx / len * width / 2f;
        WorldRenderer wr = start(GL11.GL_QUADS);
        v(wr, side * (ax + nx), ay + ny, z, colorA);
        v(wr, side * (bx + nx), by + ny, z, colorB);
        v(wr, side * (bx - nx), by - ny, z, colorB);
        v(wr, side * (ax - nx), ay - ny, z, colorA);
        draw();
    }

    private static void angelWing(int side, float ticks) {
        membrane(side, outline(0.8f), argb(0xFFFFFF, 0.95f), argb(0xE4EEFF, 0.85f), 0f);
        // two rows of long feathers fanning out, with a soft blue tint at the tips
        for (int row = 0; row < 2; row++) {
            int count = row == 0 ? 9 : 6;
            float reach = row == 0 ? 1.0f : 0.6f;
            for (int i = 0; i < count; i++) {
                float t = i / (float) (count - 1);
                float angle = (float) Math.toRadians(-52 + t * 128);
                float len = reach * (0.62f + 0.3f * (float) Math.sin(t * Math.PI));
                float tipX = (float) Math.sin(angle + Math.PI / 2) * len;
                float tipY = -(float) Math.cos(angle + Math.PI / 2) * len;
                strip(side, 0.04f, 0f, tipX, tipY, 0.11f - row * 0.03f,
                        argb(0xFFFFFF, 1f), argb(row == 0 ? 0xCFE2FF : 0xF4F8FF, 0.95f), 0.006f * (row + 1));
            }
        }
    }

    private static void dragonWing(int side) {
        membrane(side, outline(0.62f), argb(0x8E1426, 0.95f), argb(0x3A0610, 0.9f), 0f);
        for (float[] bone : BONES) {
            strip(side, 0f, 0f, bone[0], bone[1], 0.045f, argb(0x1A0306, 1f), argb(0x2A060B, 1f), 0.004f);
        }
        // a little claw at the top bone
        float[] top = BONES[0];
        strip(side, top[0], top[1], top[0] + 0.08f, top[1] - 0.1f, 0.035f, argb(0xD8C8B0, 1f), argb(0xFFFFFF, 1f), 0.004f);
    }

    private static void crystalWing(int side) {
        int accent = NovaClient.accent() & 0xFFFFFF;
        additive(true);
        membrane(side, outline(0.72f), argb(accent, 0.55f), argb(accent, 0.18f), 0f);
        float[][] edge = outline(0.72f);
        for (int i = 0; i + 1 < edge.length; i++) {
            strip(side, edge[i][0], edge[i][1], edge[i + 1][0], edge[i + 1][1], 0.025f, argb(0xFFFFFF, 0.85f), argb(0xFFFFFF, 0.85f), 0.003f);
        }
        for (float[] bone : BONES) {
            strip(side, 0f, 0f, bone[0], bone[1], 0.02f, argb(0xFFFFFF, 0.7f), argb(accent, 0.5f), 0.003f);
        }
        additive(false);
    }

    // ------------------------------------------------------------------ head

    /** A glowing ring floating above the head, bobbing gently. */
    private static void halo(Pose pose) {
        GlStateManager.rotate(pose.headYaw, 0f, 1f, 0f);
        float bob = (float) Math.sin(pose.ticks * 0.1f) * 0.025f;
        GlStateManager.translate(0f, -0.66f - bob, 0f);
        additive(true);
        ring(0.15f, 0.36f, argb(0xFFE9A0, 0f), argb(0xFFD866, 0.35f), argb(0xFFE9A0, 0f));   // soft glow
        ring(0.2f, 0.27f, argb(0xFFF6D0, 0.95f), argb(0xFFFFFF, 1f), argb(0xFFF6D0, 0.95f)); // the ring itself
        additive(false);
    }

    /** A flat ring (in the horizontal plane) whose color fades from inner, to middle, to outer edge. */
    private static void ring(float inner, float outer, int innerColor, int midColor, int outerColor) {
        float mid = (inner + outer) / 2f;
        int segments = 48;
        for (int band = 0; band < 2; band++) {
            float r1 = band == 0 ? inner : mid, r2 = band == 0 ? mid : outer;
            int c1 = band == 0 ? innerColor : midColor, c2 = band == 0 ? midColor : outerColor;
            WorldRenderer wr = start(GL11.GL_TRIANGLE_STRIP);
            for (int i = 0; i <= segments; i++) {
                double a = i * Math.PI * 2 / segments;
                float cos = (float) Math.cos(a), sin = (float) Math.sin(a);
                v(wr, cos * r1, 0, sin * r1, c1);
                v(wr, cos * r2, 0, sin * r2, c2);
            }
            draw();
        }
    }

    /** A gold crown with eight points and gems, sitting on top of the head. */
    private static void crown(Pose pose) {
        GlStateManager.rotate(pose.headYaw, 0f, 1f, 0f);
        GlStateManager.rotate(pose.headPitch, 1f, 0f, 0f);
        GlStateManager.translate(0f, -0.5f, 0f);
        int sides = 8;
        float r = 0.23f, bottom = 0.02f, top = -0.08f, peak = -0.19f;
        int[] gems = {0xE0202E, 0x2E7BFF, 0x2EC46A, 0x9B3DFF};
        for (int i = 0; i < sides; i++) {
            double a1 = i * Math.PI * 2 / sides, a2 = (i + 1) * Math.PI * 2 / sides, am = (a1 + a2) / 2;
            float x1 = (float) Math.cos(a1) * r, z1 = (float) Math.sin(a1) * r;
            float x2 = (float) Math.cos(a2) * r, z2 = (float) Math.sin(a2) * r;
            float shade = 0.75f + 0.25f * (float) Math.cos(am - 0.8);
            int gold = shadeColor(0xF2C230, shade);
            int goldDark = shadeColor(0xB8860B, shade);

            WorldRenderer wr = start(GL11.GL_QUADS);   // band
            v(wr, x1, bottom, z1, argb(goldDark, 1f));
            v(wr, x2, bottom, z2, argb(goldDark, 1f));
            v(wr, x2, top, z2, argb(gold, 1f));
            v(wr, x1, top, z1, argb(gold, 1f));
            draw();

            wr = start(GL11.GL_TRIANGLES);             // point
            float pxm = (float) Math.cos(am) * r, pzm = (float) Math.sin(am) * r;
            v(wr, x1, top, z1, argb(gold, 1f));
            v(wr, x2, top, z2, argb(gold, 1f));
            v(wr, pxm, peak, pzm, argb(shadeColor(0xFFE680, shade), 1f));
            draw();

            // a gem on every other side, pushed slightly outward so it sits on the band
            if (i % 2 == 0) {
                float gr = r + 0.006f, gy = (bottom + top) / 2f, gs = 0.035f;
                float gx = (float) Math.cos(am) * gr, gz = (float) Math.sin(am) * gr;
                float tx = -(float) Math.sin(am) * gs, tz = (float) Math.cos(am) * gs;
                int gem = argb(gems[(i / 2) % gems.length], 1f);
                wr = start(GL11.GL_QUADS);
                v(wr, gx, gy - gs, gz, gem);
                v(wr, gx + tx, gy, gz + tz, gem);
                v(wr, gx, gy + gs, gz, gem);
                v(wr, gx - tx, gy, gz - tz, gem);
                draw();
            }
        }
    }

    private static int shadeColor(int rgb, float f) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 255) * f));
        int g = Math.min(255, Math.round(((rgb >> 8) & 255) * f));
        int b = Math.min(255, Math.round((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }

    // ------------------------------------------------------------------ aura

    /** Little glowing stars circling the player at different heights. */
    private static void aura(String style, Pose pose) {
        boolean rainbow = style.equals("rainbow_stars");
        int accent = NovaClient.accent() & 0xFFFFFF;
        int count = 7;
        additive(true);
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(pose.ticks * 4f + i * (360.0 / count));
            float radius = 0.62f + 0.06f * (float) Math.sin(pose.ticks * 0.07f + i);
            float x = (float) Math.cos(angle) * radius, z = (float) Math.sin(angle) * radius;
            float y = 0.55f + (float) Math.sin(pose.ticks * 0.06f + i * 1.3f) * 0.55f;
            int color = rainbow ? Color.HSBtoRGB((i / (float) count + pose.ticks * 0.005f) % 1f, 0.7f, 1f) & 0xFFFFFF : accent;
            float twinkle = 0.7f + 0.3f * (float) Math.sin(pose.ticks * 0.3f + i * 2);
            diamond(x, y, z, 0.11f, argb(color, 0.25f * twinkle));   // glow
            diamond(x, y, z, 0.045f, argb(0xFFFFFF, 0.95f * twinkle)); // bright center
            diamond(x, y, z, 0.06f, argb(color, 0.8f * twinkle));
        }
        additive(false);
    }

    /** A small 3D diamond (octahedron) that looks like a sparkle from any angle. */
    private static void diamond(float x, float y, float z, float s, int color) {
        float[][] tips = {{s, 0, 0}, {0, 0, s}, {-s, 0, 0}, {0, 0, -s}};
        WorldRenderer wr = start(GL11.GL_TRIANGLES);
        for (int i = 0; i < 4; i++) {
            float[] a = tips[i], b = tips[(i + 1) % 4];
            for (int dir = -1; dir <= 1; dir += 2) {
                v(wr, x, y + dir * s * 1.4f, z, color);
                v(wr, x + a[0], y, z + a[2], color);
                v(wr, x + b[0], y, z + b[2], color);
            }
        }
        draw();
    }
}
