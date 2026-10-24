package com.nova.gui;

import com.nova.NovaClient;
import com.nova.emote.Emote;
import com.nova.emote.EmotePlayer;
import com.nova.font.Fonts;
import com.nova.util.Anim;
import com.nova.util.Render;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * Hold the emote key, point at an emote, let go to play it (like Lunar).
 * A quick tap keeps the wheel open instead, so you can click one.
 */
public class EmoteWheel extends GuiScreen {
    private static final float OUTER = 96f;
    private static final float INNER = 32f;
    private static final long TAP_MS = 250;

    private final KeyBinding key;
    private final boolean turnCamera;
    private final Anim open = new Anim(14f, 0f);
    private final Ui.Hovers hovers = new Ui.Hovers();
    private final long openedAt = System.currentTimeMillis();
    private boolean keyWasReleased;

    public EmoteWheel(KeyBinding key, boolean turnCamera) {
        this.key = key;
        this.turnCamera = turnCamera;
    }

    @Override
    public void initGui() {
        open.setTarget(1f);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private boolean keyHeld() {
        int code = key.getKeyCode();
        if (code == 0) return false;
        return code < 0 ? Mouse.isButtonDown(code + 100) : Keyboard.isKeyDown(code);
    }

    /** Which emote the mouse points at, or -1 in the middle. Emote 0 is at the top, going clockwise. */
    private int hovered(int mouseX, int mouseY) {
        float dx = mouseX - width / 2f, dy = mouseY - height / 2f;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < INNER) return -1;
        double deg = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        int count = Emote.values().length;
        return (int) Math.round(((deg % 360) + 360) % 360 / (360.0 / count)) % count;
    }

    private void choose(int index) {
        mc.displayGuiScreen(null);
        if (index >= 0) EmotePlayer.play(Emote.values()[index], turnCamera);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // let go of the key to play the emote you're pointing at
        if (!keyHeld()) {
            if (System.currentTimeMillis() - openedAt > TAP_MS && !keyWasReleased) {
                choose(hovered(mouseX, mouseY));
                return;
            }
            keyWasReleased = true;
        }

        int accent = NovaClient.accent();
        float o = Anim.easeOut(open.get());
        float cx = width / 2f, cy = height / 2f;
        Render.rect(0, 0, width, height, Render.alpha(0x60000000, o));

        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, 0);
        GlStateManager.scale(0.85f + 0.15f * o, 0.85f + 0.15f * o, 1f);
        GlStateManager.translate(-cx, -cy, 0);

        Emote[] emotes = Emote.values();
        float step = 360f / emotes.length;
        int hover = hovered(mouseX, mouseY);
        Render.arc(cx, cy, OUTER, OUTER - INNER, 0, 360, Render.alpha(0xE0101016, o));
        for (int i = 0; i < emotes.length; i++) {
            float h = hovers.get(emotes[i], i == hover);
            if (h > 0.01f) {
                Render.arc(cx, cy, OUTER + h * 4, OUTER - INNER + h * 4, i * step - step / 2f + 1f, step - 2f, Render.alpha(accent, 0.85f * h * o));
            }
            // thin dividers between the slices
            Render.arc(cx, cy, OUTER, OUTER - INNER, i * step - step / 2f - 0.4f, 0.8f, Render.alpha(0x30FFFFFF, o));
            double rad = Math.toRadians(i * step - 90);
            float r = (OUTER + INNER) / 2f + h * 2f;
            float lx = cx + (float) Math.cos(rad) * r, ly = cy + (float) Math.sin(rad) * r;
            Fonts.MEDIUM.drawCentered(emotes[i].displayName, lx, ly - Fonts.MEDIUM.getHeight() / 2f, Render.alpha(Render.mix(Ui.TEXT_DIM, Ui.TEXT, h), o));
        }
        Render.arc(cx, cy, OUTER, 1f, 0, 360, Render.alpha(0x40FFFFFF, o));

        // the middle shows what you'll play
        Render.circle(cx, cy, INNER, Render.alpha(0xF0181820, o));
        String center = hover >= 0 ? emotes[hover].displayName : "EMOTES";
        Fonts.SMALL.drawCentered(center, cx, cy - Fonts.SMALL.getHeight() / 2f, Render.alpha(hover >= 0 ? accent : Ui.TEXT_DIM, o));
        GlStateManager.popMatrix();

        String hint = keyWasReleased ? "Click an emote  |  Move to stop" : "Let go to play  |  Move to stop";
        Fonts.SMALL.drawCentered(hint, cx, cy + OUTER + 14, Render.alpha(Ui.TEXT_DIM, o));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 0) choose(hovered(mouseX, mouseY));
        else choose(-1);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == key.getKeyCode()) choose(-1);
    }
}
