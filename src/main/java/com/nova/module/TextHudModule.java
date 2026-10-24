package com.nova.module;

import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import net.minecraft.client.gui.Gui;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * The easiest kind of mod: a box with text (FPS, CPS, ping...).
 * Implement {@link #getText()} for one line, or override {@link #getLines()} for several.
 * Background, brackets and color settings come for free.
 */
public abstract class TextHudModule extends HudModule {
    private static final int PADDING = 4;
    private static final int HEIGHT = 16;
    private static final int LINE_H = 10;
    private static final int MIN_WIDTH = 56;

    protected final BooleanSetting background = add(new BooleanSetting("Background", true));
    protected final BooleanSetting brackets = add(new BooleanSetting("Brackets", false));
    protected final ModeSetting color = add(new ModeSetting("Text color",
            "White", "White", "Aqua", "Green", "Yellow", "Red", "Pink", "Chroma"));

    protected TextHudModule(String name, String description, Category category, int defaultX, int defaultY) {
        super(name, description, category, defaultX, defaultY);
    }

    /** The text to show, or null to show nothing right now. */
    protected String getText() {
        return null;
    }

    /** What to show in the HUD editor when {@link #getText()} returns null. */
    protected String getPreviewText() {
        return getName();
    }

    /** Lines to show, top to bottom. By default this is just {@link #getText()}. */
    protected List<String> getLines() {
        String text = getText();
        return text == null ? null : Collections.singletonList(text);
    }

    protected List<String> getPreviewLines() {
        return Collections.singletonList(getPreviewText());
    }

    private List<String> currentLines(boolean preview) {
        List<String> lines = mc.thePlayer == null ? null : getLines();
        if ((lines == null || lines.isEmpty()) && preview) lines = getPreviewLines();
        return lines == null || lines.isEmpty() ? null : lines;
    }

    private String decorate(String line) {
        return brackets.isOn() ? "[" + line + "]" : line;
    }

    @Override
    public int getWidth() {
        List<String> lines = currentLines(true);
        int textWidth = 0;
        for (String line : lines) textWidth = Math.max(textWidth, mc.fontRendererObj.getStringWidth(decorate(line)));
        textWidth += PADDING * 2;
        return background.isOn() && lines.size() == 1 ? Math.max(MIN_WIDTH, textWidth) : textWidth;
    }

    @Override
    public int getHeight() {
        List<String> lines = currentLines(true);
        return lines.size() == 1 ? HEIGHT : lines.size() * LINE_H + PADDING * 2 - 2;
    }

    @Override
    public void render(boolean preview) {
        List<String> lines = currentLines(preview);
        if (lines == null) return;
        int width = getWidth();
        if (background.isOn()) {
            Gui.drawRect(0, 0, width, getHeight(), 0x6F000000);
        }
        if (lines.size() == 1) {
            String text = decorate(lines.get(0));
            int textX = (width - mc.fontRendererObj.getStringWidth(text)) / 2;
            mc.fontRendererObj.drawStringWithShadow(text, textX, (HEIGHT - 8) / 2f, getTextColor());
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            mc.fontRendererObj.drawStringWithShadow(decorate(lines.get(i)), PADDING, PADDING + i * LINE_H, getTextColor());
        }
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
