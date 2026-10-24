package com.nova.gui;

import com.nova.NovaClient;
import com.nova.font.Fonts;
import com.nova.setting.BooleanSetting;
import com.nova.setting.KeybindSetting;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.setting.Setting;
import com.nova.util.Anim;
import com.nova.util.Render;
import org.lwjgl.input.Keyboard;

import java.util.List;

/** Draws a list of settings as rows with switches, sliders, mode pickers and key buttons. */
public final class SettingsList {
    public static final int ROW_H = 22;
    public static final int GAP = 4;
    private static final int SWITCH_W = 24;
    private static final int MODE_W = 96;
    private static final int TRACK_W = 100;
    private static final int KEY_W = 74;

    private final Ui.Hovers hovers = new Ui.Hovers();
    private final Ui.Hovers switches = new Ui.Hovers();
    private long revealStart;
    private NumberSetting dragging;
    private float dragTrackX;
    private KeybindSetting listening;

    public static float height(List<Setting> settings) {
        return settings.size() * (ROW_H + GAP);
    }

    public boolean isListening() {
        return listening != null;
    }

    public void draw(List<Setting> settings, float x, float y, float w, int mouseX, int mouseY) {
        int accent = NovaClient.accent();
        for (int i = 0; i < settings.size(); i++) {
            float appear = Anim.cascade(revealStart, i);
            drawRow(settings.get(i), x, y + i * (ROW_H + GAP) + (1f - appear) * 8f, w, mouseX, mouseY, accent);
            if (appear < 1f) {
                Render.roundedRect(x, y + i * (ROW_H + GAP) + (1f - appear) * 8f, w, ROW_H, 4, Render.alpha(Ui.PANEL_SOLID, 1f - appear));
            }
        }
    }

    /** Makes the rows slide in one after another, starting now. */
    public void reveal() {
        revealStart = System.currentTimeMillis();
    }

    private void drawRow(Setting setting, float x, float ry, float w, int mouseX, int mouseY, int accent) {
        float hover = hovers.get(setting, Ui.isInside(mouseX, mouseY, x, ry, w, ROW_H));
        Render.roundedRect(x, ry, w, ROW_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover));
        Fonts.REGULAR.draw(setting.getName(), x + 10, Fonts.REGULAR.centerY(ry, ROW_H), Ui.TEXT);
        float right = x + w - 10;

        if (setting instanceof BooleanSetting) {
            float on = switches.state(setting, ((BooleanSetting) setting).isOn());
            float sx = right - SWITCH_W;
            float sy = ry + (ROW_H - 12) / 2f;
            Render.roundedRect(sx, sy, SWITCH_W, 12, 6, Render.mix(Ui.TRACK, accent, on));
            Render.circle(sx + 6 + on * (SWITCH_W - 12), sy + 6, 4.5f, Ui.TEXT);
        } else if (setting instanceof ModeSetting) {
            float mx = right - MODE_W;
            Render.roundedRect(mx, ry + 4, MODE_W, ROW_H - 8, 3, Ui.SURFACE_DARK);
            Fonts.REGULAR.draw("<", mx + 6, Fonts.REGULAR.centerY(ry, ROW_H), Ui.TEXT_DIM);
            Fonts.REGULAR.draw(">", mx + MODE_W - 6 - Fonts.REGULAR.getWidth(">"), Fonts.REGULAR.centerY(ry, ROW_H), Ui.TEXT_DIM);
            Fonts.REGULAR.drawCentered(setting.getDisplayValue(), mx + MODE_W / 2f, Fonts.REGULAR.centerY(ry, ROW_H), accent);
        } else if (setting instanceof NumberSetting) {
            NumberSetting number = (NumberSetting) setting;
            float tx = right - TRACK_W;
            float ty = ry + ROW_H / 2f - 1.5f;
            float fill = (float) number.getPercent() * TRACK_W;
            Render.roundedRect(tx, ty, TRACK_W, 3, 1.5f, Ui.TRACK);
            Render.roundedRect(tx, ty, fill, 3, 1.5f, accent);
            Render.circle(tx + fill, ty + 1.5f, dragging == number ? 5f : 4f, Ui.TEXT);
            String value = number.getDisplayValue();
            Fonts.REGULAR.draw(value, tx - 10 - Fonts.REGULAR.getWidth(value), Fonts.REGULAR.centerY(ry, ROW_H), Ui.TEXT_DIM);
        } else if (setting instanceof KeybindSetting) {
            float kx = right - KEY_W;
            boolean active = listening == setting;
            Render.roundedRect(kx, ry + 4, KEY_W, ROW_H - 8, 3, active ? Render.alpha(accent, 0.35f) : Ui.SURFACE_DARK);
            String text = active ? "Press a key..." : setting.getDisplayValue();
            Fonts.REGULAR.drawCentered(text, kx + KEY_W / 2f, Fonts.REGULAR.centerY(ry, ROW_H), active ? Ui.TEXT : accent);
        }
    }

    /** @return true if a setting changed. */
    public boolean mouseClicked(List<Setting> settings, float x, float y, float w, int mouseX, int mouseY, int button) {
        listening = null;
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            float ry = y + i * (ROW_H + GAP);
            if (!Ui.isInside(mouseX, mouseY, x, ry, w, ROW_H)) continue;
            float right = x + w - 10;

            if (setting instanceof NumberSetting) {
                float tx = right - TRACK_W;
                if (mouseX >= tx - 6 && mouseX <= tx + TRACK_W + 6) {
                    dragging = (NumberSetting) setting;
                    dragTrackX = tx;
                    dragging.setPercent((mouseX - tx) / TRACK_W);
                    return true;
                }
                return false;
            }
            if (setting instanceof KeybindSetting) {
                listening = (KeybindSetting) setting;
                return false;
            }
            if (setting instanceof ModeSetting) {
                float mx = right - MODE_W;
                setting.click(button == 1 || mouseX < mx + MODE_W / 2f && mouseX >= mx ? 1 : 0);
                return true;
            }
            setting.click(button);
            return true;
        }
        return false;
    }

    /** @return true if a slider moved. */
    public boolean mouseDragged(int mouseX) {
        if (dragging == null) return false;
        dragging.setPercent((mouseX - dragTrackX) / TRACK_W);
        return true;
    }

    public void mouseReleased() {
        dragging = null;
    }

    /** @return true if the key was used to set a keybind. */
    public boolean keyTyped(int keyCode) {
        if (listening == null) return false;
        listening.setKey(keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK ? Keyboard.KEY_NONE : keyCode);
        listening = null;
        return true;
    }
}
