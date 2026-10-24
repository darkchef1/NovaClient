package com.nova.gui;

import com.nova.NovaClient;
import com.nova.font.Fonts;
import com.nova.module.HudModule;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.KeybindSetting;
import com.nova.setting.NumberSetting;
import com.nova.setting.Setting;
import com.nova.util.Anim;
import com.nova.util.Blur;
import com.nova.util.Render;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Right Shift menu, laid out like Lunar's: tabs on top, filter chips and search,
 * a grid of mod cards, and a settings page per mod.
 * <p>
 * Everything animates: the panel scales and fades in and out, cards cascade in one
 * after another, hover lifts cards, and colors fade instead of switching instantly.
 */
public class ClickGuiScreen extends GuiScreen {
    private enum Tab {MODS, SETTINGS}

    private static final String[] FILTERS = {"ALL", "ENABLED", "HUD", "PVP", "MECHANIC", "CHAT"};
    private static final int HEADER_H = 34;
    private static final int TOOLBAR_H = 18;
    private static final int CARD_H = 86;
    private static final int CARD_MIN_W = 104;
    private static final int GAP = 8;

    private final Ui.Hovers hovers = new Ui.Hovers();
    private final Ui.Hovers states = new Ui.Hovers();
    private final SettingsList settingsList = new SettingsList();
    private final Anim open = new Anim(10f, 0f);
    private final Anim scroll = new Anim(14f, 0f);

    private Tab tab = Tab.MODS;
    private String filter = "ALL";
    private String search = "";
    private boolean searchFocused;
    private boolean closing;
    private long revealStart = System.currentTimeMillis();
    private Module page;
    private List<Setting> pageSettings;
    private BooleanSetting pageEnabled;
    private NumberSetting pageSize;
    private float maxScroll;

    // layout, recalculated every frame
    private float px, py, pw, ph;

    public ClickGuiScreen() {
    }

    /** Opens the menu directly on a mod's settings page. */
    public ClickGuiScreen(Module module) {
        openPage(module);
    }

    @Override
    public void initGui() {
        if (!closing) open.setTarget(1f);
        Keyboard.enableRepeatEvents(true);
        if (NovaClient.getSettings().menuBlur.isOn()) Blur.start();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        Blur.stop();
        NovaClient.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /** Plays the closing animation, then closes the menu. */
    private void close() {
        closing = true;
        open.setTarget(0f);
    }

    /** Restarts the cascade so the content slides in again (after changing tab, filter or page). */
    private void reveal() {
        revealStart = System.currentTimeMillis();
        settingsList.reveal();
        scroll.snap(0);
    }

    private void layout() {
        pw = Math.min(width - 24, 460);
        ph = Math.min(height - 24, 290);
        px = (width - pw) / 2f;
        py = (height - ph) / 2f;
    }

    private float contentX() {
        return px + 12;
    }

    private float contentY() {
        return py + HEADER_H + TOOLBAR_H + 16;
    }

    private float contentW() {
        return pw - 24;
    }

    private float contentH() {
        return py + ph - 10 - contentY();
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        layout();
        float o = open.get();
        if (closing && o < 0.03f) {
            mc.displayGuiScreen(null);
            return;
        }
        float eased = Anim.easeOut(o);
        float scale = 0.92f + 0.08f * eased;

        Render.verticalGradient(0, 0, width, height, Render.alpha(0x88000000, eased), Render.alpha(0xAA000000, eased));

        // scale the panel around the screen center; map the mouse into that space too
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-width / 2f, -height / 2f, 0);
        int mx = (int) (width / 2f + (mouseX - width / 2f) / scale);
        int my = (int) (height / 2f + (mouseY - height / 2f) / scale);
        if (closing) mx = my = -1;

        Render.roundedRect(px, py, pw, ph, 6, Ui.PANEL);
        drawHeader(mx, my);

        Render.clip(contentX() - 2, contentY() - 2, contentW() + 4, contentH() + 4);
        float offset = scroll.get();
        int contentMouseY = inContent(mx, my) ? my : -1;
        if (tab == Tab.SETTINGS) {
            List<Setting> settings = NovaClient.getSettings().getSettings();
            settingsList.draw(settings, contentX(), contentY() - offset, contentW(), mx, contentMouseY);
            maxScroll = Math.max(0, SettingsList.height(settings) - contentH());
        } else if (page != null) {
            syncPage();
            settingsList.draw(pageSettings, contentX(), contentY() - offset, contentW(), mx, contentMouseY);
            maxScroll = Math.max(0, SettingsList.height(pageSettings) - contentH());
        } else {
            drawCards(mx, contentMouseY, offset);
        }
        Render.endClip();
        drawScrollbar();

        // fade the whole panel in and out
        if (eased < 1f) Render.roundedRect(px, py, pw, ph, 6, Render.alpha(Ui.PANEL_SOLID, 1f - eased));
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHeader(int mouseX, int mouseY) {
        int accent = NovaClient.accent();
        float logoY = Fonts.LARGE.centerY(py, HEADER_H + 4);
        float after = Fonts.LARGE.draw("NOVA", px + 14, logoY, accent);
        Fonts.LARGE.draw(" CLIENT", after, logoY, Ui.TEXT);

        // tabs
        float tabX = px + pw / 2f - 70;
        for (Tab t : Tab.values()) {
            String label = t.name();
            float hover = hovers.get("tab" + label, Ui.isInside(mouseX, mouseY, tabX, py + 10, 66, 18));
            float active = states.state("tab" + label, tab == t);
            int bg = Render.mix(Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover), Render.alpha(accent, 0.55f), active);
            Render.roundedRect(tabX, py + 10, 66, 18, 4, bg);
            Fonts.MEDIUM.drawCentered(label, tabX + 33, Fonts.MEDIUM.centerY(py + 10, 18), Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(hover, active)));
            tabX += 72;
        }

        // edit HUD + close
        float closeX = px + pw - 28;
        float editX = closeX - 104;
        float editHover = hovers.get("edit", Ui.isInside(mouseX, mouseY, editX, py + 10, 98, 18));
        Render.roundedRect(editX, py + 10 - editHover, 98, 18, 4, Render.mix(accent, 0xFFFFFFFF, editHover * 0.15f));
        Fonts.MEDIUM.drawCentered("EDIT HUD LAYOUT", editX + 49, Fonts.MEDIUM.centerY(py + 10 - editHover, 18), Ui.TEXT);

        float closeHover = hovers.get("close", Ui.isInside(mouseX, mouseY, closeX, py + 10, 18, 18));
        Render.roundedRect(closeX, py + 10, 18, 18, 4, Render.mix(Ui.SURFACE, Ui.OFF, closeHover));
        Fonts.MEDIUM.drawCentered("x", closeX + 9, Fonts.MEDIUM.centerY(py + 9, 18), Ui.TEXT);

        Render.rect(px + 12, py + HEADER_H + 2, pw - 24, 1, 0x20FFFFFF);

        float barY = py + HEADER_H + 9;
        if (tab == Tab.SETTINGS) {
            Fonts.LARGE.draw("Client Settings", px + 12, Fonts.LARGE.centerY(barY, TOOLBAR_H), Ui.TEXT);
        } else if (page != null) {
            drawPageBar(mouseX, mouseY, barY);
        } else {
            drawFilterBar(mouseX, mouseY, barY);
        }
    }

    private void drawFilterBar(int mouseX, int mouseY, float y) {
        int accent = NovaClient.accent();
        float x = px + 12;
        for (String f : FILTERS) {
            float w = Fonts.MEDIUM.getWidth(f) + 16;
            float hover = hovers.get("filter" + f, Ui.isInside(mouseX, mouseY, x, y, w, TOOLBAR_H));
            float active = states.state("filter" + f, filter.equals(f));
            Render.roundedRect(x, y, w, TOOLBAR_H, 4, Render.mix(Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover), accent, active));
            Fonts.MEDIUM.drawCentered(f, x + w / 2f, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(hover, active)));
            x += w + 5;
        }

        float sw = Math.max(60, Math.min(130, px + pw - 12 - x - 6));
        float sx = px + pw - 12 - sw;
        float focus = states.state("search", searchFocused);
        Render.roundedRect(sx, y, sw, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, focus));
        if (focus > 0.01f) Render.outline(sx, y, sw, TOOLBAR_H, 0.5f, Render.alpha(accent, 0.8f * focus));
        float textY = Fonts.REGULAR.centerY(y, TOOLBAR_H);
        if (search.isEmpty() && !searchFocused) {
            Fonts.REGULAR.draw("Search...", sx + 7, textY, Ui.TEXT_DIM);
        } else {
            float end = Fonts.REGULAR.draw(search, sx + 7, textY, Ui.TEXT);
            float blink = (float) (Math.sin(System.currentTimeMillis() / 160.0) * 0.5 + 0.5);
            if (searchFocused) Render.rect(end + 1, y + 4, 0.75f, TOOLBAR_H - 8, Render.alpha(Ui.TEXT, blink));
        }
    }

    private void drawPageBar(int mouseX, int mouseY, float y) {
        float backHover = hovers.get("back", Ui.isInside(mouseX, mouseY, px + 12, y, 52, TOOLBAR_H));
        Render.roundedRect(px + 12, y, 52, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, backHover));
        Fonts.MEDIUM.drawCentered("< BACK", px + 38 - backHover * 2, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Ui.TEXT);

        float nameEnd = Fonts.LARGE.draw(page.getName(), px + 74, Fonts.LARGE.centerY(y, TOOLBAR_H), Ui.TEXT);
        Fonts.REGULAR.draw(page.getDescription(), nameEnd + 8, Fonts.REGULAR.centerY(y, TOOLBAR_H) + 1, Ui.TEXT_DIM);

        if (page instanceof HudModule) {
            float rx = px + pw - 12 - 100;
            float h = hovers.get("reset", Ui.isInside(mouseX, mouseY, rx, y, 100, TOOLBAR_H));
            Render.roundedRect(rx, y, 100, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, h));
            Fonts.MEDIUM.drawCentered("RESET POSITION", rx + 50, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Ui.TEXT);
        }
    }

    private int columns() {
        return Math.max(2, (int) ((contentW() + GAP) / (CARD_MIN_W + GAP)));
    }

    private float cardW() {
        int cols = columns();
        return (contentW() - (cols - 1) * GAP) / cols;
    }

    private void drawCards(int mouseX, int mouseY, float offset) {
        List<Module> mods = visibleModules();
        int cols = columns();
        float cw = cardW();
        int rows = (mods.size() + cols - 1) / cols;
        maxScroll = Math.max(0, rows * (CARD_H + GAP) - GAP - contentH());

        if (mods.isEmpty()) {
            Fonts.REGULAR.drawCentered("No mods found", contentX() + contentW() / 2f, contentY() + 30, Ui.TEXT_DIM);
            return;
        }
        for (int i = 0; i < mods.size(); i++) {
            float x = contentX() + (i % cols) * (cw + GAP);
            float y = contentY() + (i / cols) * (CARD_H + GAP) - offset;
            if (y + CARD_H < contentY() - 2 || y > contentY() + contentH() + 2) continue;
            float appear = Anim.cascade(revealStart, i);
            float slide = (1f - appear) * 10f;
            drawCard(mods.get(i), x, y + slide, cw, mouseX, appear < 1f ? -1 : mouseY);
            if (appear < 1f) Render.roundedRect(x, y + slide - 2, cw, CARD_H + 4, 5, Render.alpha(Ui.PANEL_SOLID, 1f - appear));
        }
    }

    private void drawCard(Module module, float x, float y, float w, int mouseX, int mouseY) {
        float hover = hovers.get(module, Ui.isInside(mouseX, mouseY, x, y, w, CARD_H));
        float optionsHover = hovers.get(module.getName() + "#opt", Ui.isInside(mouseX, mouseY, x, y + CARD_H - 34, w, 16));
        float statusHover = hovers.get(module.getName() + "#status", Ui.isInside(mouseX, mouseY, x, y + CARD_H - 18, w, 18));
        float on = states.state(module.getName() + "#on", module.isEnabled());

        // hover lifts the card a little and shows a soft shadow
        float lift = hover * 2f;
        Render.roundedRect(x + 1, y + 3 - lift, w - 2, CARD_H, 5, Render.alpha(0x60000000, hover));
        y -= lift;

        Render.roundedRect(x, y, w, CARD_H, 5, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover));
        Render.item(module.getIcon(), x + w / 2f - 12 - hover * 1.5f, y + 9 - hover * 1.5f, 1.5f + hover * 0.12f);
        Fonts.MEDIUM.drawCentered(module.getName(), x + w / 2f, y + 38, Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(on, hover)));

        // OPTIONS row
        float oy = y + CARD_H - 34;
        Render.rect(x, oy, w, 16, Render.alpha(0x30FFFFFF, optionsHover));
        Render.rect(x + 6, oy, w - 12, 0.5f, 0x22FFFFFF);
        Fonts.SMALL.drawCentered("OPTIONS", x + (w - 18) / 2f, Fonts.SMALL.centerY(oy, 16), Render.mix(Ui.TEXT_DIM, Ui.TEXT, optionsHover));
        Render.rect(x + w - 18, oy + 3, 0.5f, 10, 0x22FFFFFF);
        Render.arc(x + w - 9, oy + 8, 3.5f, 1.4f, optionsHover * 90f, 360, Render.mix(Ui.TEXT_DIM, Ui.TEXT, optionsHover));

        // ENABLED / DISABLED bar fades between red and green
        float sy = y + CARD_H - 18;
        int status = Render.mix(Render.mix(Ui.OFF, Ui.ON, on), 0xFFFFFFFF, statusHover * 0.12f);
        Render.roundedRect(x, sy, w, 18, 5, status);
        Render.rect(x, sy, w, 6, status);
        Fonts.MEDIUM.drawCentered(module.isEnabled() ? "ENABLED" : "DISABLED", x + w / 2f, Fonts.MEDIUM.centerY(sy, 18), Ui.TEXT);
    }

    private void drawScrollbar() {
        if (maxScroll <= 0) return;
        float trackH = contentH();
        float barH = Math.max(20, trackH * trackH / (trackH + maxScroll));
        float barY = contentY() + (trackH - barH) * (scroll.get() / maxScroll);
        Render.roundedRect(px + pw - 7, barY, 2.5f, barH, 1.25f, 0x50FFFFFF);
    }

    private boolean inContent(int mouseX, int mouseY) {
        return Ui.isInside(mouseX, mouseY, contentX() - 2, contentY() - 2, contentW() + 4, contentH() + 4);
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        String query = search.trim().toLowerCase();
        for (Module module : NovaClient.getModuleManager().getModules()) {
            if (!query.isEmpty() && !module.getName().toLowerCase().contains(query)) continue;
            if (filter.equals("ALL")
                    || filter.equals("ENABLED") && module.isEnabled()
                    || module.getCategory().name().equals(filter)) {
                result.add(module);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ settings page

    private void openPage(Module module) {
        page = module;
        pageEnabled = new BooleanSetting("Enabled", module.isEnabled());
        pageSettings = new ArrayList<>();
        pageSettings.add(pageEnabled);
        pageSettings.add(new KeybindSetting(module));
        pageSize = null;
        if (module instanceof HudModule) {
            pageSize = new NumberSetting("Size", ((HudModule) module).getScale(), 0.5, 2.5, 0.05, "x");
            pageSettings.add(pageSize);
        }
        pageSettings.addAll(module.getSettings());
        reveal();
    }

    /** Pushes the page's helper rows (Enabled, Size) into the actual mod. */
    private void syncPage() {
        if (pageEnabled.isOn() != page.isEnabled()) page.setEnabled(pageEnabled.isOn());
        if (pageSize != null) {
            HudModule hud = (HudModule) page;
            if (Math.abs(hud.getScale() - pageSize.getFloat()) > 0.001f) hud.setScale(pageSize.getFloat());
        }
    }

    // ------------------------------------------------------------------ input

    /** Mouse position inside the (possibly still scaling) panel. */
    private int mapX(int mouseX) {
        float scale = 0.92f + 0.08f * Anim.easeOut(open.get());
        return (int) (width / 2f + (mouseX - width / 2f) / scale);
    }

    private int mapY(int mouseY) {
        float scale = 0.92f + 0.08f * Anim.easeOut(open.get());
        return (int) (height / 2f + (mouseY - height / 2f) / scale);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (closing) return;
        layout();
        mouseX = mapX(mouseX);
        mouseY = mapY(mouseY);
        searchFocused = false;

        if (clickHeader(mouseX, mouseY)) return;
        if (!inContent(mouseX, mouseY)) return;

        float offset = scroll.get();
        if (tab == Tab.SETTINGS) {
            if (settingsList.mouseClicked(NovaClient.getSettings().getSettings(), contentX(), contentY() - offset, contentW(), mouseX, mouseY, button)) {
                NovaClient.saveConfig();
            }
            return;
        }
        if (page != null) {
            if (settingsList.mouseClicked(pageSettings, contentX(), contentY() - offset, contentW(), mouseX, mouseY, button)) {
                syncPage();
                NovaClient.saveConfig();
            }
            return;
        }

        List<Module> mods = visibleModules();
        int cols = columns();
        float cw = cardW();
        for (int i = 0; i < mods.size(); i++) {
            float x = contentX() + (i % cols) * (cw + GAP);
            float y = contentY() + (i / cols) * (CARD_H + GAP) - offset;
            if (!Ui.isInside(mouseX, mouseY, x, y, cw, CARD_H)) continue;
            Module module = mods.get(i);
            boolean options = Ui.isInside(mouseX, mouseY, x, y + CARD_H - 34, cw, 16);
            if (options || button == 1) {
                openPage(module);
            } else {
                module.toggle();
                NovaClient.saveConfig();
            }
            return;
        }
    }

    private boolean clickHeader(int mouseX, int mouseY) {
        float tabX = px + pw / 2f - 70;
        for (Tab t : Tab.values()) {
            if (Ui.isInside(mouseX, mouseY, tabX, py + 10, 66, 18)) {
                if (tab != t) {
                    tab = t;
                    reveal();
                }
                return true;
            }
            tabX += 72;
        }
        float closeX = px + pw - 28;
        if (Ui.isInside(mouseX, mouseY, closeX, py + 10, 18, 18)) {
            close();
            return true;
        }
        if (Ui.isInside(mouseX, mouseY, closeX - 104, py + 10, 98, 18)) {
            mc.displayGuiScreen(new HudEditorScreen());
            return true;
        }

        float barY = py + HEADER_H + 9;
        if (tab != Tab.MODS) return false;
        if (page != null) {
            if (Ui.isInside(mouseX, mouseY, px + 12, barY, 52, TOOLBAR_H)) {
                page = null;
                reveal();
                return true;
            }
            if (page instanceof HudModule && Ui.isInside(mouseX, mouseY, px + pw - 112, barY, 100, TOOLBAR_H)) {
                ((HudModule) page).resetPosition();
                pageSize.set(1);
                NovaClient.saveConfig();
                return true;
            }
            return false;
        }

        float x = px + 12;
        for (String f : FILTERS) {
            float w = Fonts.MEDIUM.getWidth(f) + 16;
            if (Ui.isInside(mouseX, mouseY, x, barY, w, TOOLBAR_H)) {
                if (!filter.equals(f)) {
                    filter = f;
                    reveal();
                }
                return true;
            }
            x += w + 5;
        }
        if (Ui.isInside(mouseX, mouseY, x, barY, px + pw - 12 - x, TOOLBAR_H)) {
            searchFocused = true;
            return true;
        }
        return false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (settingsList.mouseDragged(mapX(mouseX)) && page != null) syncPage();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (settingsList.isListening()) return;
        settingsList.mouseReleased();
        NovaClient.saveConfig();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            float target = scroll.getTarget() - Math.signum(wheel) * 40f;
            scroll.setTarget(Math.max(0, Math.min(maxScroll, target)));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (closing) return;
        if (settingsList.keyTyped(keyCode)) {
            NovaClient.saveConfig();
            return;
        }
        if (keyCode == NovaClient.MENU_KEY.getKeyCode() && !searchFocused) {
            close();
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (page != null) {
                page = null;
                reveal();
            } else if (searchFocused || !search.isEmpty()) {
                search = "";
                searchFocused = false;
                reveal();
            } else {
                close();
            }
            return;
        }
        if (tab != Tab.MODS || page != null) return;

        if (keyCode == Keyboard.KEY_BACK) {
            if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
            scroll.snap(0);
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && search.length() < 24) {
            if (typedChar == ' ' && search.isEmpty()) return;
            searchFocused = true;
            search += typedChar;
            scroll.snap(0);
        }
    }
}
