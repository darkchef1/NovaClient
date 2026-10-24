package com.nova.gui;

import com.nova.NovaClient;
import com.nova.font.Fonts;
import com.nova.util.Anim;
import com.nova.util.Render;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.GuiModList;

import java.io.IOException;
import java.util.Random;

/**
 * Nova's title screen: an animated night-sky background, the Nova logo, and a column of buttons
 * that slide in one after another. Replaces the vanilla main menu (see ClientEvents.onGuiOpen).
 */
public class NovaMainMenu extends GuiScreen {
    private static final String[] BUTTONS = {"Singleplayer", "Multiplayer", "Settings", "Client", "Quit Game"};
    private static final int BUTTON_W = 170;
    private static final int BUTTON_H = 22;
    private static final int BUTTON_GAP = 6;
    private static final float BUTTON_RADIUS = 7f;

    private static final int STAR_COUNT = 90;
    private static final float[][] STARS = new float[STAR_COUNT][];
    private static final long START = System.currentTimeMillis();

    static {
        Random random = new Random(1337);
        for (int i = 0; i < STAR_COUNT; i++) {
            // x, y (0-1), size, twinkle phase, depth (for parallax and drift speed)
            STARS[i] = new float[]{random.nextFloat(), random.nextFloat(), 0.5f + random.nextFloat() * 1.2f,
                    random.nextFloat() * 6.28f, 0.3f + random.nextFloat() * 0.7f};
        }
    }

    private final Ui.Hovers hovers = new Ui.Hovers();
    /** How far the 3D player is spun around; it eases back to facing you after you let go. */
    private final Anim spin = new Anim(4f, 0f);
    private boolean draggingPlayer;
    private int lastDragX;
    private long openedAt;

    @Override
    public void initGui() {
        openedAt = System.currentTimeMillis();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ------------------------------------------------------------------ background

    /** The animated background. Also drawn behind the mod menu when it's opened from here. */
    public static void drawBackground(int width, int height, int mouseX, int mouseY) {
        float t = (System.currentTimeMillis() - START) / 1000f;
        int accent = NovaClient.accent();
        float parallaxX = (mouseX - width / 2f) / width;
        float parallaxY = (mouseY - height / 2f) / height;

        Render.verticalGradient(0, 0, width, height, 0xFF07070D, 0xFF120E22);

        // three slow-moving colored glows
        float big = Math.max(width, height) * 0.45f;
        Render.glow(width * 0.28f + (float) Math.sin(t * 0.13) * width * 0.06f - parallaxX * 25,
                height * 0.35f + (float) Math.cos(t * 0.11) * height * 0.06f - parallaxY * 25, big, accent, 0.9f);
        Render.glow(width * 0.75f + (float) Math.cos(t * 0.09) * width * 0.07f - parallaxX * 40,
                height * 0.65f + (float) Math.sin(t * 0.12) * height * 0.07f - parallaxY * 40, big * 0.9f, 0xFF7A3CFF, 0.8f);
        Render.glow(width * 0.55f + (float) Math.sin(t * 0.07) * width * 0.08f - parallaxX * 15,
                height * 0.15f - parallaxY * 15, big * 0.6f, 0xFF2BB5FF, 0.5f);

        // stars drifting slowly upward and twinkling
        for (float[] s : STARS) {
            float depth = s[4];
            float y = ((s[1] - t * 0.004f * depth) % 1f + 1f) % 1f;
            float x = s[0] * width - parallaxX * 30 * depth;
            float twinkle = (float) Math.sin(t * 1.6f + s[3]) * 0.5f + 0.5f;
            float alpha = 0.25f + 0.75f * twinkle * depth;
            Render.circle(x, y * height - parallaxY * 30 * depth, s[2] * depth, Render.alpha(0xFFFFFFFF, alpha));
        }

        // darken the edges a little
        Render.verticalGradient(0, 0, width, height * 0.25f, 0x70000000, 0x00000000);
        Render.verticalGradient(0, height * 0.75f, width, height * 0.25f, 0x00000000, 0x90000000);
    }

    // ------------------------------------------------------------------ layout
    // The logo and buttons are one block, drawn at (0, 0) = its top center, then scaled to fit the screen.

    private static final float LOGO_CENTER = 24;
    private static final float BUTTONS_TOP = 108;
    private static final float BLOCK_H = BUTTONS_TOP + BUTTONS.length * (BUTTON_H + BUTTON_GAP) - BUTTON_GAP;
    /** The 3D player stands this far right of the block's center, and is this many pixels per block tall. */
    private static final float PLAYER_X = BUTTON_W / 2f + 70;
    private static final float PLAYER_SIZE = 64;

    /** How much to shrink the block so it fits between the corner texts. */
    private float blockScale() {
        return Math.min(1f, (height - 56f) / BLOCK_H);
    }

    private float blockTop() {
        return (height - BLOCK_H * blockScale()) / 2f;
    }

    /** Mouse position inside the block's own coordinates. */
    private float localX(int mouseX) {
        return (mouseX - width / 2f) / blockScale();
    }

    private float localY(int mouseY) {
        return (mouseY - blockTop()) / blockScale();
    }

    private static float buttonY(int index) {
        return BUTTONS_TOP + index * (BUTTON_H + BUTTON_GAP);
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(width, height, mouseX, mouseY);
        long since = System.currentTimeMillis() - openedAt;
        int accent = NovaClient.accent();
        float lx = localX(mouseX);
        float ly = localY(mouseY);

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, blockTop(), 0);
        GlStateManager.scale(blockScale(), blockScale(), 1f);

        drawLogo(Anim.easeOut(since / 600f));

        for (int i = 0; i < BUTTONS.length; i++) {
            float appear = Anim.easeOut((since - 250 - i * 60) / 350f);
            if (appear <= 0) continue;
            float x = -BUTTON_W / 2f;
            float y = buttonY(i) + (1f - appear) * 14f;
            boolean hovered = appear >= 1f && Ui.isInside(lx, ly, x, y, BUTTON_W, BUTTON_H);
            float h = hovers.get(BUTTONS[i], hovered);

            Render.roundedRect(x, y, BUTTON_W, BUTTON_H, BUTTON_RADIUS, Render.alpha(0x14FFFFFF, appear * (1f + h * 1.5f)));
            Render.roundedOutline(x, y, BUTTON_W, BUTTON_H, BUTTON_RADIUS, 0.5f, Render.alpha(0x26FFFFFF, appear * (1f + h * 2f)));
            if (h > 0.01f) Render.roundedRect(x + 4, y + 6, 2f, BUTTON_H - 12, 1f, Render.alpha(accent, h));
            int textColor = Render.alpha(Render.mix(0xFFC8C8D2, 0xFFFFFFFF, h), appear);
            Fonts.MEDIUM.drawCentered(BUTTONS[i], h * 2f, Fonts.MEDIUM.centerY(y, BUTTON_H), textColor);
        }
        drawPlayer(lx, ly, Anim.easeOut((since - 450) / 500f));
        GlStateManager.popMatrix();

        drawCorners(mouseX, mouseY, Anim.easeOut((since - 500) / 500f));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLogo(float appear) {
        if (appear <= 0) return;
        float t = (System.currentTimeMillis() - START) / 1000f;
        int accent = NovaClient.accent();
        float cx = 0;
        float cy = LOGO_CENTER + (1f - appear) * 10f;
        float pulse = (float) Math.sin(t * 2f) * 0.5f + 0.5f;
        float size = appear;

        Render.glow(cx, cy, 46 * size, accent, appear * (0.8f + pulse * 0.4f));
        Render.star(cx, cy, 20 * size, 4 * size, 4, t * 12f, Render.alpha(0xFFFFFFFF, appear));
        Render.star(cx, cy, 11 * size, 3 * size, 4, t * 12f + 45f, Render.alpha(accent, appear * 0.9f));
        Render.circle(cx, cy, 2.5f * size, Render.alpha(0xFFFFFFFF, appear));

        Fonts.HUGE.drawSpaced("NOVA", cx, cy + 28, 5f, Render.alpha(0xFFFFFFFF, appear));
        Fonts.SMALL.drawSpaced("CLIENT", cx, cy + 28 + Fonts.HUGE.getHeight() + 1, 4f, Render.alpha(accent, appear));
    }

    /** Your skin in 3D, standing to the right of the buttons. Drag it to spin it around. */
    private void drawPlayer(float lx, float ly, float appear) {
        if (appear <= 0) return;
        float feetY = BLOCK_H + (1f - appear) * 12f;
        float headY = feetY - PLAYER_SIZE * 1.62f;

        Render.glow(PLAYER_X, feetY - PLAYER_SIZE * 0.95f, PLAYER_SIZE * 1.15f, NovaClient.accent(), 0.55f * appear);
        GlStateManager.pushMatrix();
        GlStateManager.translate(PLAYER_X, feetY, 0);
        GlStateManager.scale(1f, 0.22f, 1f);
        Render.glow(0, 0, PLAYER_SIZE * 0.5f, 0xFF000000, 1.6f * appear);
        GlStateManager.popMatrix();

        if (!draggingPlayer) spin.setTarget(0f);
        PlayerPreview.draw(PLAYER_X, feetY, PLAYER_SIZE, spin.get(), lx - PLAYER_X, ly - headY);

        String name = mc.getSession().getUsername();
        Fonts.REGULAR.drawCentered(name, PLAYER_X, headY - PLAYER_SIZE * 0.42f - 10, Render.alpha(0xFFFFFFFF, appear));
    }

    private static boolean onPlayer(float lx, float ly) {
        return Ui.isInside(lx, ly, PLAYER_X - PLAYER_SIZE * 0.45f, BLOCK_H - PLAYER_SIZE * 2.05f, PLAYER_SIZE * 0.9f, PLAYER_SIZE * 2.05f);
    }

    private void drawCorners(int mouseX, int mouseY, float appear) {
        if (appear <= 0) return;
        int dim = Render.alpha(Ui.TEXT_DIM, appear);

        // top left: who you're logged in as, and the active profile
        Render.circle(10, 12, 2.5f, Render.alpha(Ui.ON, appear));
        Fonts.REGULAR.draw(mc.getSession().getUsername(), 16, Fonts.REGULAR.centerY(6, 12), Render.alpha(Ui.TEXT, appear));
        Fonts.SMALL.draw("Profile: " + NovaClient.getConfig().getActiveProfile(), 8, 22, dim);

        // bottom left: version
        Fonts.SMALL.draw("Nova Client 1.0  |  Minecraft 1.8.9", 8, height - 12, dim);

        // bottom right: Forge mods and language
        float x = width - 8;
        for (String label : new String[]{"Language", "Mods"}) {
            float w = Fonts.REGULAR.getWidth(label);
            x -= w;
            float h = hovers.get("corner" + label, Ui.isInside(mouseX, mouseY, x - 3, height - 16, w + 6, 14));
            Fonts.REGULAR.draw(label, x, height - 13, Render.alpha(Render.mix(Ui.TEXT_DIM, Ui.TEXT, h), appear));
            Render.rect(x, height - 3, w * h, 0.75f, Render.alpha(NovaClient.accent(), appear));
            x -= 14;
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button != 0) return;
        if (onPlayer(localX(mouseX), localY(mouseY))) {
            draggingPlayer = true;
            lastDragX = mouseX;
            return;
        }
        for (int i = 0; i < BUTTONS.length; i++) {
            if (!Ui.isInside(localX(mouseX), localY(mouseY), -BUTTON_W / 2f, buttonY(i), BUTTON_W, BUTTON_H)) continue;
            switch (i) {
                case 0: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
                case 1: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
                case 2: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
                case 3: mc.displayGuiScreen(new ClickGuiScreen()); break;
                default: mc.shutdown();
            }
            return;
        }

        float x = width - 8;
        for (String label : new String[]{"Language", "Mods"}) {
            float w = Fonts.REGULAR.getWidth(label);
            x -= w;
            if (Ui.isInside(mouseX, mouseY, x - 3, height - 16, w + 6, 14)) {
                if (label.equals("Mods")) mc.displayGuiScreen(new GuiModList(this));
                else mc.displayGuiScreen(new GuiLanguage(this, mc.gameSettings, mc.getLanguageManager()));
                return;
            }
            x -= 14;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (!draggingPlayer) return;
        spin.snap(spin.get() + (mouseX - lastDragX) * 3f);
        lastDragX = mouseX;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingPlayer = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // ESC does nothing on the title screen, like vanilla
    }
}
