package com.nova.gui;

import com.nova.NovaClient;
import com.nova.event.ClientEvents;
import com.nova.font.Fonts;
import com.nova.module.HudModule;
import com.nova.util.Anim;
import com.nova.util.Render;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Drag, snap and resize the enabled HUD mods.
 * Elements glide to their new spot, snap to the screen center, edges and each other,
 * and can be resized from the corner handle or with the scroll wheel.
 */
public class HudEditorScreen extends GuiScreen {
    private static final float SNAP_DISTANCE = 4f;
    private static final float HANDLE = 6f;
    private static final int BUTTON_W = 74;
    private static final int BUTTON_H = 18;
    private static final String[] BUTTONS = {"MODS", "RESET ALL", "SNAP"};

    private final Ui.Hovers hovers = new Ui.Hovers();
    private final Map<HudModule, float[]> shown = new HashMap<>();
    private final Anim toolbar = new Anim(12f, 0f);
    private final List<Float> guidesX = new ArrayList<>();
    private final List<Float> guidesY = new ArrayList<>();
    private long lastFrame = System.nanoTime();

    private HudModule selected;
    private HudModule dragging;
    private HudModule resizing;
    private float dragOffsetX;
    private float dragOffsetY;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        NovaClient.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private List<HudModule> enabledHud() {
        List<HudModule> result = new ArrayList<>();
        for (HudModule module : NovaClient.getModuleManager().getHudModules()) {
            if (module.isEnabled()) result.add(module);
        }
        return result;
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int accent = NovaClient.accent();
        long now = System.nanoTime();
        float delta = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;

        Render.rect(0, 0, width, height, 0x38000000);
        Render.rect(width / 2f - 0.25f, 0, 0.5f, height, 0x14FFFFFF);
        Render.rect(0, height / 2f - 0.25f, width, 0.5f, 0x14FFFFFF);

        boolean busy = dragging != null || resizing != null;
        HudModule hovered = busy ? null : findAt(mouseX, mouseY);

        for (HudModule module : enabledHud()) {
            float[] pos = glide(module, delta);
            float w = module.getScaledWidth();
            float h = module.getScaledHeight();
            boolean active = module == selected || module == dragging || module == resizing;
            float hover = hovers.get(module, module == hovered || active);

            Render.rect(pos[0], pos[1], w, h, Render.alpha(0x22FFFFFF, hover));
            ClientEvents.renderHud(module, pos[0], pos[1], true);
            Render.outline(pos[0], pos[1], w, h, 0.5f, active ? accent : Render.mix(0x35FFFFFF, 0xA0FFFFFF, hover));

            if (hover > 0.01f) {
                Render.rect(pos[0] + w - HANDLE / 2f, pos[1] + h - HANDLE / 2f, HANDLE, HANDLE, Render.alpha(accent, hover));
                String label = module.getName() + "  " + String.format("%.2fx", module.getScale());
                float lw = Fonts.SMALL.getWidth(label) + 8;
                float ly = pos[1] > 14 ? pos[1] - 12 : pos[1] + h + 2;
                Render.roundedRect(pos[0], ly, lw, 10, 3, Render.alpha(0xE0101016, hover));
                Fonts.SMALL.draw(label, pos[0] + 4, Fonts.SMALL.centerY(ly, 10), Render.alpha(0xFFFFFFFF, hover));
            }
        }

        for (float gx : guidesX) Render.rect(gx - 0.25f, 0, 0.5f, height, accent);
        for (float gy : guidesY) Render.rect(0, gy - 0.25f, width, 0.5f, accent);

        drawToolbar(mouseX, mouseY, busy);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** Smoothly moves the drawn position toward the real one. */
    private float[] glide(HudModule module, float delta) {
        float tx = module.getX();
        float ty = module.getY();
        float[] pos = shown.get(module);
        if (pos == null) {
            pos = new float[]{tx, ty};
            shown.put(module, pos);
        }
        float t = Math.min(1f, delta * (module == dragging ? 32f : 16f));
        pos[0] += (tx - pos[0]) * t;
        pos[1] += (ty - pos[1]) * t;
        if (Math.abs(tx - pos[0]) < 0.05f) pos[0] = tx;
        if (Math.abs(ty - pos[1]) < 0.05f) pos[1] = ty;
        return pos;
    }

    private float toolbarX() {
        return width / 2f - (BUTTONS.length * (BUTTON_W + 6) - 6) / 2f;
    }

    private float toolbarY() {
        return height / 2f - BUTTON_H / 2f;
    }

    private void drawToolbar(int mouseX, int mouseY, boolean busy) {
        toolbar.setTarget(busy ? 0f : 1f);
        float alpha = toolbar.get();
        if (alpha < 0.02f) return;
        int accent = NovaClient.accent();

        float x = toolbarX();
        float y = toolbarY();
        for (String label : BUTTONS) {
            float h = hovers.get("btn" + label, Ui.isInside(mouseX, mouseY, x, y, BUTTON_W, BUTTON_H));
            int bg = label.equals("MODS") ? accent : Ui.SURFACE;
            Render.roundedRect(x, y, BUTTON_W, BUTTON_H, 4, Render.alpha(Render.mix(bg, 0xFFFFFFFF, h * 0.15f), alpha));
            String text = label.equals("SNAP") ? (NovaClient.getSettings().snapping.isOn() ? "SNAP: ON" : "SNAP: OFF") : label;
            Fonts.MEDIUM.drawCentered(text, x + BUTTON_W / 2f, Fonts.MEDIUM.centerY(y, BUTTON_H), Render.alpha(0xFFFFFFFF, alpha));
            x += BUTTON_W + 6;
        }
        String hint = "Drag to move  |  Corner or scroll to resize  |  Right-click for settings  |  Hold ALT to stop snapping";
        Fonts.SMALL.drawCentered(hint, width / 2f, y + BUTTON_H + 6, Render.alpha(Ui.TEXT_DIM, alpha));
    }

    // ------------------------------------------------------------------ input

    private HudModule findAt(float mouseX, float mouseY) {
        List<HudModule> modules = enabledHud();
        for (int i = modules.size() - 1; i >= 0; i--) {
            HudModule m = modules.get(i);
            if (Ui.isInside(mouseX, mouseY, m.getX() - 2, m.getY() - 2, m.getScaledWidth() + 4, m.getScaledHeight() + 4)) {
                return m;
            }
        }
        return null;
    }

    private boolean onHandle(HudModule m, float mouseX, float mouseY) {
        float hx = m.getX() + m.getScaledWidth();
        float hy = m.getY() + m.getScaledHeight();
        return Math.abs(mouseX - hx) <= HANDLE && Math.abs(mouseY - hy) <= HANDLE;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 0 && toolbar.get() > 0.5f && clickToolbar(mouseX, mouseY)) return;

        if (button == 0) {
            List<HudModule> modules = enabledHud();
            for (int i = modules.size() - 1; i >= 0; i--) {
                if (onHandle(modules.get(i), mouseX, mouseY)) {
                    resizing = selected = modules.get(i);
                    return;
                }
            }
        }

        HudModule hit = findAt(mouseX, mouseY);
        if (button == 1 && hit != null) {
            mc.displayGuiScreen(new ClickGuiScreen(hit));
            return;
        }
        selected = hit;
        if (button == 0 && hit != null) {
            dragging = hit;
            dragOffsetX = mouseX - hit.getX();
            dragOffsetY = mouseY - hit.getY();
        }
    }

    private boolean clickToolbar(int mouseX, int mouseY) {
        float x = toolbarX();
        float y = toolbarY();
        for (String label : BUTTONS) {
            if (Ui.isInside(mouseX, mouseY, x, y, BUTTON_W, BUTTON_H)) {
                if (label.equals("MODS")) {
                    mc.displayGuiScreen(new ClickGuiScreen());
                } else if (label.equals("RESET ALL")) {
                    for (HudModule module : NovaClient.getModuleManager().getHudModules()) module.resetPosition();
                    NovaClient.saveConfig();
                } else {
                    NovaClient.getSettings().snapping.click(0);
                    NovaClient.saveConfig();
                }
                return true;
            }
            x += BUTTON_W + 6;
        }
        return false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (resizing != null) {
            float byX = (mouseX - resizing.getX()) / resizing.getWidth();
            float byY = (mouseY - resizing.getY()) / resizing.getHeight();
            float scale = Math.round(Math.max(byX, byY) * 20f) / 20f;
            resizing.setScale(scale);
            return;
        }
        if (dragging == null) return;

        float x = mouseX - dragOffsetX;
        float y = mouseY - dragOffsetY;
        guidesX.clear();
        guidesY.clear();
        boolean snap = NovaClient.getSettings().snapping.isOn() && !Keyboard.isKeyDown(Keyboard.KEY_LMENU);
        if (snap) {
            List<Float> linesX = new ArrayList<>();
            List<Float> linesY = new ArrayList<>();
            linesX.add(0f);
            linesX.add(width / 2f);
            linesX.add((float) width);
            linesY.add(0f);
            linesY.add(height / 2f);
            linesY.add((float) height);
            for (HudModule other : enabledHud()) {
                if (other == dragging) continue;
                float ox = other.getX(), oy = other.getY(), ow = other.getScaledWidth(), oh = other.getScaledHeight();
                linesX.add(ox);
                linesX.add(ox + ow / 2f);
                linesX.add(ox + ow);
                linesY.add(oy);
                linesY.add(oy + oh / 2f);
                linesY.add(oy + oh);
            }
            x = snap(x, dragging.getScaledWidth(), linesX, guidesX);
            y = snap(y, dragging.getScaledHeight(), linesY, guidesY);
        }
        dragging.setPosition(x, y);
    }

    /** Moves pos so its left/center/right edge lines up with the closest nearby line. */
    private static float snap(float pos, float size, List<Float> lines, List<Float> guides) {
        float best = SNAP_DISTANCE + 1;
        float bestLine = 0;
        float[] points = {0f, size / 2f, size};
        for (float line : lines) {
            for (float point : points) {
                float d = line - (pos + point);
                if (Math.abs(d) < Math.abs(best)) {
                    best = d;
                    bestLine = line;
                }
            }
        }
        if (Math.abs(best) <= SNAP_DISTANCE) {
            guides.add(bestLine);
            return pos + best;
        }
        return pos;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging != null || resizing != null) NovaClient.saveConfig();
        dragging = null;
        resizing = null;
        guidesX.clear();
        guidesY.clear();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        HudModule hit = findAt(mouseX, mouseY);
        if (hit != null) {
            hit.setScale(Math.round((hit.getScale() + Math.signum(wheel) * 0.05f) * 20f) / 20f);
            NovaClient.saveConfig();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new ClickGuiScreen());
            return;
        }
        if (selected == null) return;
        float step = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10f : 1f;
        float x = selected.getX();
        float y = selected.getY();
        switch (keyCode) {
            case Keyboard.KEY_LEFT: selected.setPosition(x - step, y); break;
            case Keyboard.KEY_RIGHT: selected.setPosition(x + step, y); break;
            case Keyboard.KEY_UP: selected.setPosition(x, y - step); break;
            case Keyboard.KEY_DOWN: selected.setPosition(x, y + step); break;
            case Keyboard.KEY_DELETE:
                selected.setEnabled(false);
                selected = null;
                break;
            default: return;
        }
        NovaClient.saveConfig();
    }
}
