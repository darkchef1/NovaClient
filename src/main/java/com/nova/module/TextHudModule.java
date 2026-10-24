package com.nova.module;

import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import net.minecraft.client.gui.Gui;

import java.awt.Color;

/**
 * The easiest kind of mod: a box with one line of text (FPS, CPS, ping...).
 * Just implement {@link #getText()}. Background, brackets and color settings come for free.
 */
public abstract class TextHudModule extends HudModule {
    private static final int PADDING = 4;
    private static final int HEIGHT = 16;
    private static final int MIN_WIDTH = 56;

    protected final BooleanSetting background = add(new BooleanSetting("Background", true));
    protected final BooleanSetting brackets = add(new BooleanSetting("Brackets", false));
    protected final ModeSetting color = add(new ModeSetting("Text color",
            "White", "White", "Aqua", "Green", "Yellow", "Red", "Pink", "Chroma"));

    protected TextHudModule(String name, String description, Category category, int defaultX, int defaultY) {
        super(name, description, category, defaultX, defaultY);
    }

    /** The text to show, or null to show nothing right now. */
    protected abstract String getText();

    /** What to show in the HUD editor when {@link #getText()} returns null. */
    protected String getPreviewText() {
        return getName();
    }

    private String currentText(boolean preview) {
        String text = mc.thePlayer == null ? null : getText();
        if (text == null && preview) text = getPreviewText();
        if (text == null) return null;
        return brackets.isOn() ? "[" + text + "]" : text;
    }

    @Override
    public int getWidth() {
        String text = currentText(true);
        int textWidth = mc.fontRendererObj.getStringWidth(text) + PADDING * 2;
        return background.isOn() ? Math.max(MIN_WIDTH, textWidth) : textWidth;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(boolean preview) {
        String text = currentText(preview);
        if (text == null) return;
        int width = getWidth();
        if (background.isOn()) {
            Gui.drawRect(0, 0, width, HEIGHT, 0x6F000000);
        }
        int textX = (width - mc.fontRendererObj.getStringWidth(text)) / 2;
        mc.fontRendererObj.drawStringWithShadow(text, textX, (HEIGHT - 8) / 2f, getTextColor());
    }

    protected int getTextColor() {
        switch (color.getMode()) {
            case "Aqua": return 0xFF55FFFF;
            case "Green": return 0xFF55FF55;
            case "Yellow": return 0xFFFFFF55;
            case "Red": return 0xFFFF5555;
            case "Pink": return 0xFFFF55FF;
            case "Chroma": return Color.HSBtoRGB((System.currentTimeMillis() % 3000) / 3000f, 0.7f, 1f);
            default: return 0xFFFFFFFF;
        }
    }
}
