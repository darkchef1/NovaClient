package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.Anim;
import com.nova.util.CpsTracker;
import com.nova.util.Render;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** WASD, mouse buttons and space bar. Keys fade smoothly when pressed and released. */
public class Keystrokes extends HudModule {
    private static final int KEY = 22;
    private static final int GAP = 2;
    private static final int WIDTH = KEY * 3 + GAP * 2;
    private static final int MOUSE_WIDTH = (WIDTH - GAP) / 2;
    private static final int SPACE_HEIGHT = 10;
    private static final int IDLE_BG = 0x70000000;

    private final BooleanSetting showMouse = add(new BooleanSetting("Show mouse buttons", true));
    private final BooleanSetting showCps = add(new BooleanSetting("Show CPS", true));
    private final BooleanSetting showSpace = add(new BooleanSetting("Show space bar", true));
    private final ModeSetting pressedColor = add(new ModeSetting("Pressed color", "White", "White", "Accent", "Chroma"));
    private final NumberSetting fadeSpeed = add(new NumberSetting("Fade speed", 12, 3, 30, 1));
    private final BooleanSetting rounded = add(new BooleanSetting("Rounded corners", true));
    private final BooleanSetting pop = add(new BooleanSetting("Press animation", true));

    private final Map<KeyBinding, Anim> fades = new HashMap<>();

    public Keystrokes() {
        super("Keystrokes", "Shows which keys and mouse buttons you press", Category.HUD, 2, 100);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.sign);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        int height = KEY * 2 + GAP;
        if (showMouse.isOn()) height += KEY + GAP;
        if (showSpace.isOn()) height += SPACE_HEIGHT + GAP;
        return height;
    }

    /** How "pressed" a key looks right now, from 0 (up) to 1 (down), fading between them. */
    private float pressAmount(KeyBinding key) {
        Anim anim = fades.get(key);
        if (anim == null) {
            anim = new Anim(fadeSpeed.getFloat(), 0f);
            fades.put(key, anim);
        }
        anim.setSpeed(fadeSpeed.getFloat());
        anim.setTarget(key.isKeyDown() ? 1f : 0f);
        return anim.get();
    }

    @Override
    public void render(boolean preview) {
        GameSettings s = mc.gameSettings;
        drawKey(s.keyBindForward, KEY + GAP, 0, KEY, KEY, null);
        drawKey(s.keyBindLeft, 0, KEY + GAP, KEY, KEY, null);
        drawKey(s.keyBindBack, KEY + GAP, KEY + GAP, KEY, KEY, null);
        drawKey(s.keyBindRight, (KEY + GAP) * 2, KEY + GAP, KEY, KEY, null);

        int y = (KEY + GAP) * 2;
        if (showMouse.isOn()) {
            String left = showCps.isOn() ? CpsTracker.getLeftCps() + " CPS" : null;
            String right = showCps.isOn() ? CpsTracker.getRightCps() + " CPS" : null;
            drawKey(s.keyBindAttack, 0, y, MOUSE_WIDTH, KEY, left);
            drawKey(s.keyBindUseItem, MOUSE_WIDTH + GAP, y, MOUSE_WIDTH, KEY, right);
            y += KEY + GAP;
        }
        if (showSpace.isOn()) {
            float t = pressAmount(s.keyBindJump);
            drawBox(0, y, WIDTH, SPACE_HEIGHT, t);
            int bar = Render.mix(0xFFFFFFFF, 0xFF000000, t);
            Render.rect(WIDTH / 2f - 12, y + 4, 24, 2, bar);
        }
    }

    private void drawKey(KeyBinding key, int x, int y, int w, int h, String subText) {
        float t = pressAmount(key);
        drawBox(x, y, w, h, t);

        int textColor = Render.mix(0xFFFFFFFF, 0xFF000000, t * pressedTextDarkness());
        String label = keyName(key.getKeyCode());
        float labelY = subText == null ? y + (h - 8) / 2f : y + 4;
        mc.fontRendererObj.drawString(label, x + (w - mc.fontRendererObj.getStringWidth(label)) / 2f, labelY, textColor, false);

        if (subText != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + w / 2f, y + 14, 0);
            GlStateManager.scale(0.5f, 0.5f, 1f);
            mc.fontRendererObj.drawString(subText, -mc.fontRendererObj.getStringWidth(subText) / 2f, 0, textColor, false);
            GlStateManager.popMatrix();
        }
    }

    /** The key's background, fading from dark to the pressed color, with a small pop when pressed. */
    private void drawBox(float x, float y, float w, float h, float t) {
        float grow = pop.isOn() ? (float) Math.sin(t * Math.PI) * 1f : 0f;
        int color = Render.mix(IDLE_BG, pressedBackground(), t);
        float radius = rounded.isOn() ? 3f : 0f;
        Render.roundedRect(x - grow, y - grow, w + grow * 2, h + grow * 2, radius, color);
    }

    private int pressedBackground() {
        if (pressedColor.is("Accent")) return Render.alpha(NovaClient.accent(), 0.85f);
        if (pressedColor.is("Chroma")) return Render.alpha(Render.chroma(0), 0.85f);
        return 0xC8FFFFFF;
    }

    /** White keys need dark text when pressed; colored ones keep white text. */
    private float pressedTextDarkness() {
        return pressedColor.is("White") ? 1f : 0f;
    }

    private static String keyName(int code) {
        if (code == -100) return "LMB";
        if (code == -99) return "RMB";
        return GameSettings.getKeyDisplayString(code);
    }
}
