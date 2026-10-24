package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import com.nova.util.CpsTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class Keystrokes extends HudModule {
    private static final int KEY = 22;
    private static final int GAP = 2;
    private static final int WIDTH = KEY * 3 + GAP * 2;
    private static final int MOUSE_WIDTH = (WIDTH - GAP) / 2;
    private static final int SPACE_HEIGHT = 10;

    private final BooleanSetting showMouse = add(new BooleanSetting("Show mouse buttons", true));
    private final BooleanSetting showCps = add(new BooleanSetting("Show CPS", true));
    private final BooleanSetting showSpace = add(new BooleanSetting("Show space bar", true));

    public Keystrokes() {
        super("Keystrokes", "Shows which keys and mouse buttons you press", Category.HUD, 2, 100);
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
            boolean down = s.keyBindJump.isKeyDown();
            Gui.drawRect(0, y, WIDTH, y + SPACE_HEIGHT, down ? 0xAAFFFFFF : 0x6F000000);
            Gui.drawRect(WIDTH / 2 - 12, y + 4, WIDTH / 2 + 12, y + 6, down ? 0xFF000000 : 0xFFFFFFFF);
        }
    }

    private void drawKey(KeyBinding key, int x, int y, int w, int h, String subText) {
        boolean down = key.isKeyDown();
        Gui.drawRect(x, y, x + w, y + h, down ? 0xAAFFFFFF : 0x6F000000);
        int color = down ? 0xFF000000 : 0xFFFFFFFF;

        String label = keyName(key.getKeyCode());
        int labelY = subText == null ? y + (h - 8) / 2 : y + 4;
        mc.fontRendererObj.drawString(label, x + (w - mc.fontRendererObj.getStringWidth(label)) / 2, labelY, color);

        if (subText != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + w / 2f, y + 14, 0);
            GlStateManager.scale(0.5f, 0.5f, 1f);
            mc.fontRendererObj.drawString(subText, -mc.fontRendererObj.getStringWidth(subText) / 2, 0, color);
            GlStateManager.popMatrix();
        }
    }

    private static String keyName(int code) {
        if (code == -100) return "LMB";
        if (code == -99) return "RMB";
        return GameSettings.getKeyDisplayString(code);
    }

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.sign);
    }
}
