package com.nova.gui;

import com.nova.NovaClient;
import com.nova.config.Config;
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
import com.nova.util.ServerUtil;
import com.nova.waypoint.Waypoint;
import com.nova.waypoint.WaypointManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Right Shift menu, laid out like Lunar's: profiles on the left, tabs on top,
 * filter chips and search, a grid of mod cards, a settings page per mod, and waypoints.
 * <p>
 * Everything animates: the panel scales and fades in, cards cascade in one
 * after another, hover lifts cards, and colors fade instead of switching instantly.
 */
public class ClickGuiScreen extends GuiScreen {
    private enum Tab {MODS, SETTINGS, WAYPOINTS}

    private static final String[] FILTERS = {"ALL", "ENABLED", "HUD", "PVP", "MECHANIC", "CHAT"};
    private static final int HEADER_H = 34;
    private static final int TOOLBAR_H = 18;
    private static final int SIDEBAR_W = 100;
    private static final int PROFILE_H = 18;
    private static final int CARD_H = 86;
    private static final int CARD_MIN_W = 104;
    private static final int GAP = 8;
    private static final int ROW_H = 22;
    private static final int TAB_W = 76;

    private final Ui.Hovers hovers = new Ui.Hovers();
    private final Ui.Hovers states = new Ui.Hovers();
    private final SettingsList settingsList = new SettingsList();
    private final Anim open = new Anim(16f, 0f);
    private final Anim scroll = new Anim(14f, 0f);

    private Tab tab = Tab.MODS;
    private String filter = "ALL";
    private String search = "";
    private boolean searchFocused;
    private long revealStart = System.currentTimeMillis();
    private Module page;
    private List<Setting> pageSettings;
    private BooleanSetting pageEnabled;
    private NumberSetting pageSize;
    private float maxScroll;

    /** What's being renamed (a profile name String or a Waypoint), and the text typed so far. */
    private Object editing;
    private String editText = "";

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
        open.setTarget(1f);
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

    private void close() {
        mc.displayGuiScreen(null);
    }

    /** Restarts the cascade so the content slides in again (after changing tab, filter or page). */
    private void reveal() {
        revealStart = System.currentTimeMillis();
        settingsList.reveal();
        scroll.snap(0);
    }

    private void layout() {
        pw = Math.min(width - 24, 560);
        ph = Math.min(height - 24, 300);
        px = (width - pw) / 2f;
        py = (height - ph) / 2f;
    }

    private float sidebarX() {
        return px + 10;
    }

    private float sidebarY() {
        return py + HEADER_H + 8;
    }

    private float sidebarH() {
        return py + ph - 10 - sidebarY();
    }

    private float mainX() {
        return sidebarX() + SIDEBAR_W + 10;
    }

    private float mainW() {
        return px + pw - 10 - mainX();
    }

    private float toolbarY() {
        return py + HEADER_H + 8;
    }

    private float contentX() {
        return mainX();
    }

    private float contentY() {
        return toolbarY() + TOOLBAR_H + 8;
    }

    private float contentW() {
        return mainW();
    }

    private float contentH() {
        return py + ph - 10 - contentY();
    }

    // ------------------------------------------------------------------ drawing

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        layout();
        float eased = Anim.easeOut(open.get());
        float scale = 0.92f + 0.08f * eased;

        if (mc.theWorld == null) NovaMainMenu.drawBackground(width, height, mouseX, mouseY);
        Render.verticalGradient(0, 0, width, height, Render.alpha(0x88000000, eased), Render.alpha(0xAA000000, eased));

        // scale the panel around the screen center; map the mouse into that space too
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-width / 2f, -height / 2f, 0);
        int mx = mapX(mouseX);
        int my = mapY(mouseY);

        Render.roundedRect(px, py, pw, ph, 6, Ui.PANEL);
        drawHeader(mx, my);
        drawSidebar(mx, my);
        drawToolbar(mx, my);

        Render.clip(contentX() - 2, contentY() - 2, contentW() + 4, contentH() + 4);
        float offset = scroll.get();
        int contentMouseY = inContent(mx, my) ? my : -1;
        if (tab == Tab.SETTINGS) {
            List<Setting> settings = NovaClient.getSettings().getSettings();
            settingsList.draw(settings, contentX(), contentY() - offset, contentW(), mx, contentMouseY);
            maxScroll = Math.max(0, SettingsList.height(settings) - contentH());
        } else if (tab == Tab.WAYPOINTS) {
            drawWaypoints(mx, contentMouseY, offset);
        } else if (page != null) {
            syncPage();
            settingsList.draw(pageSettings, contentX(), contentY() - offset, contentW(), mx, contentMouseY);
            maxScroll = Math.max(0, SettingsList.height(pageSettings) - contentH());
        } else {
            drawCards(mx, contentMouseY, offset);
        }
        Render.endClip();
        drawScrollbar();

        if (eased < 1f) Render.roundedRect(px, py, pw, ph, 6, Render.alpha(Ui.PANEL_SOLID, 1f - eased));
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHeader(int mouseX, int mouseY) {
        int accent = NovaClient.accent();
        float logoY = Fonts.LARGE.centerY(py, HEADER_H + 4);
        float after = Fonts.LARGE.draw("NOVA", px + 14, logoY, accent);
        Fonts.LARGE.draw(" CLIENT", after, logoY, Ui.TEXT);

        float tabX = tabsX();
        for (Tab t : Tab.values()) {
            String label = t.name();
            float hover = hovers.get("tab" + label, Ui.isInside(mouseX, mouseY, tabX, py + 10, TAB_W, 18));
            float active = states.state("tab" + label, tab == t);
            int bg = Render.mix(Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover), Render.alpha(accent, 0.55f), active);
            Render.roundedRect(tabX, py + 10, TAB_W, 18, 4, bg);
            Fonts.MEDIUM.drawCentered(label, tabX + TAB_W / 2f, Fonts.MEDIUM.centerY(py + 10, 18), Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(hover, active)));
            tabX += TAB_W + 6;
        }

        float closeX = px + pw - 28;
        float closeHover = hovers.get("close", Ui.isInside(mouseX, mouseY, closeX, py + 10, 18, 18));
        Render.roundedRect(closeX, py + 10, 18, 18, 4, Render.mix(Ui.SURFACE, Ui.OFF, closeHover));
        Fonts.MEDIUM.drawCentered("x", closeX + 9, Fonts.MEDIUM.centerY(py + 9, 18), Ui.TEXT);

        Render.rect(px + 12, py + HEADER_H + 2, pw - 24, 1, 0x20FFFFFF);
    }

    private float tabsX() {
        float total = Tab.values().length * (TAB_W + 6) - 6;
        return px + pw / 2f - total / 2f + 30;
    }

    // ---- profiles sidebar

    private void drawSidebar(int mouseX, int mouseY) {
        int accent = NovaClient.accent();
        float x = sidebarX();
        float y = sidebarY();
        Render.roundedRect(x, y, SIDEBAR_W, sidebarH(), 4, 0x60000000);
        Fonts.SMALL.draw("PROFILES", x + 7, y + 5, Ui.TEXT_DIM);

        Config config = NovaClient.getConfig();
        float rowY = y + 16;
        for (String name : config.listProfiles()) {
            if (rowY + PROFILE_H > y + sidebarH() - 44) break;
            boolean active = name.equals(config.getActiveProfile());
            boolean hovered = Ui.isInside(mouseX, mouseY, x + 4, rowY, SIDEBAR_W - 8, PROFILE_H);
            float hover = hovers.get("profile" + name, hovered);
            float on = states.state("profile" + name, active);
            Render.roundedRect(x + 4, rowY, SIDEBAR_W - 8, PROFILE_H, 3, Render.mix(Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover), Ui.SURFACE_HOVER, on));
            if (on > 0.01f) Render.rect(x + 4, rowY + 3, 2, PROFILE_H - 6, Render.alpha(accent, on));

            float textY = Fonts.REGULAR.centerY(rowY, PROFILE_H);
            if (editing != null && editing.equals(name)) {
                float end = Fonts.REGULAR.draw(editText, x + 11, textY, Ui.TEXT);
                drawCaret(end, rowY + 4, PROFILE_H - 8);
            } else {
                Fonts.REGULAR.draw(name, x + 11, textY, Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(on, hover)));
            }
            if (hovered && !active && !name.equals(Config.DEFAULT_PROFILE)) {
                Fonts.MEDIUM.draw("x", x + SIDEBAR_W - 15, textY, Ui.OFF);
            }
            rowY += PROFILE_H + 3;
        }

        float newY = y + sidebarH() - 40;
        float newHover = hovers.get("newprofile", Ui.isInside(mouseX, mouseY, x + 4, newY, SIDEBAR_W - 8, 16));
        Render.roundedRect(x + 4, newY, SIDEBAR_W - 8, 16, 3, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, newHover));
        Fonts.SMALL.drawCentered("+ NEW PROFILE", x + SIDEBAR_W / 2f, Fonts.SMALL.centerY(newY, 16), Ui.TEXT);

        float editY = y + sidebarH() - 20;
        float editHover = hovers.get("edit", Ui.isInside(mouseX, mouseY, x + 4, editY, SIDEBAR_W - 8, 16));
        Render.roundedRect(x + 4, editY - editHover, SIDEBAR_W - 8, 16, 3, Render.mix(accent, 0xFFFFFFFF, editHover * 0.15f));
        Fonts.SMALL.drawCentered("EDIT HUD LAYOUT", x + SIDEBAR_W / 2f, Fonts.SMALL.centerY(editY - editHover, 16), Ui.TEXT);
    }

    private void drawCaret(float x, float y, float h) {
        float blink = (float) (Math.sin(System.currentTimeMillis() / 160.0) * 0.5 + 0.5);
        Render.rect(x + 1, y, 0.75f, h, Render.alpha(Ui.TEXT, blink));
    }

    // ---- toolbar (filters + search, page title, or waypoint buttons)

    private void drawToolbar(int mouseX, int mouseY) {
        float y = toolbarY();
        if (tab == Tab.SETTINGS) {
            Fonts.LARGE.draw("Client Settings", mainX(), Fonts.LARGE.centerY(y, TOOLBAR_H), Ui.TEXT);
        } else if (tab == Tab.WAYPOINTS) {
            drawWaypointBar(mouseX, mouseY, y);
        } else if (page != null) {
            drawPageBar(mouseX, mouseY, y);
        } else {
            drawFilterBar(mouseX, mouseY, y);
        }
    }

    private void drawFilterBar(int mouseX, int mouseY, float y) {
        int accent = NovaClient.accent();
        float x = mainX();
        for (String f : FILTERS) {
            float w = Fonts.MEDIUM.getWidth(f) + 14;
            float hover = hovers.get("filter" + f, Ui.isInside(mouseX, mouseY, x, y, w, TOOLBAR_H));
            float active = states.state("filter" + f, filter.equals(f));
            Render.roundedRect(x, y, w, TOOLBAR_H, 4, Render.mix(Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover), accent, active));
            Fonts.MEDIUM.drawCentered(f, x + w / 2f, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(hover, active)));
            x += w + 4;
        }

        float sw = Math.max(50, Math.min(130, mainX() + mainW() - x - 4));
        float sx = mainX() + mainW() - sw;
        float focus = states.state("search", searchFocused);
        Render.roundedRect(sx, y, sw, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, focus));
        if (focus > 0.01f) Render.outline(sx, y, sw, TOOLBAR_H, 0.5f, Render.alpha(accent, 0.8f * focus));
        float textY = Fonts.REGULAR.centerY(y, TOOLBAR_H);
        if (search.isEmpty() && !searchFocused) {
            Fonts.REGULAR.draw("Search...", sx + 7, textY, Ui.TEXT_DIM);
        } else {
            float end = Fonts.REGULAR.draw(search, sx + 7, textY, Ui.TEXT);
            if (searchFocused) drawCaret(end, y + 4, TOOLBAR_H - 8);
        }
    }

    private void drawPageBar(int mouseX, int mouseY, float y) {
        float x = mainX();
        float backHover = hovers.get("back", Ui.isInside(mouseX, mouseY, x, y, 52, TOOLBAR_H));
        Render.roundedRect(x, y, 52, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, backHover));
        Fonts.MEDIUM.drawCentered("< BACK", x + 26 - backHover * 2, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Ui.TEXT);

        float nameEnd = Fonts.LARGE.draw(page.getName(), x + 62, Fonts.LARGE.centerY(y, TOOLBAR_H), Ui.TEXT);
        float resetW = page instanceof HudModule ? 104 : 0;
        String desc = page.getDescription();
        float room = mainX() + mainW() - resetW - nameEnd - 12;
        if (Fonts.REGULAR.getWidth(desc) <= room) {
            Fonts.REGULAR.draw(desc, nameEnd + 8, Fonts.REGULAR.centerY(y, TOOLBAR_H) + 1, Ui.TEXT_DIM);
        }

        if (page instanceof HudModule) {
            float rx = mainX() + mainW() - 100;
            float h = hovers.get("reset", Ui.isInside(mouseX, mouseY, rx, y, 100, TOOLBAR_H));
            Render.roundedRect(rx, y, 100, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, h));
            Fonts.MEDIUM.drawCentered("RESET POSITION", rx + 50, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Ui.TEXT);
        }
    }

    // ---- mod cards

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

        float lift = hover * 2f;
        Render.roundedRect(x + 1, y + 3 - lift, w - 2, CARD_H, 5, Render.alpha(0x60000000, hover));
        y -= lift;

        Render.roundedRect(x, y, w, CARD_H, 5, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover));
        Render.item(module.getIcon(), x + w / 2f - 12 - hover * 1.5f, y + 9 - hover * 1.5f, 1.5f + hover * 0.12f);
        Fonts.MEDIUM.drawCentered(module.getName(), x + w / 2f, y + 38, Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(on, hover)));

        float oy = y + CARD_H - 34;
        Render.rect(x, oy, w, 16, Render.alpha(0x30FFFFFF, optionsHover));
        Render.rect(x + 6, oy, w - 12, 0.5f, 0x22FFFFFF);
        Fonts.SMALL.drawCentered("OPTIONS", x + (w - 18) / 2f, Fonts.SMALL.centerY(oy, 16), Render.mix(Ui.TEXT_DIM, Ui.TEXT, optionsHover));
        Render.rect(x + w - 18, oy + 3, 0.5f, 10, 0x22FFFFFF);
        Render.arc(x + w - 9, oy + 8, 3.5f, 1.4f, optionsHover * 90f, 360, Render.mix(Ui.TEXT_DIM, Ui.TEXT, optionsHover));

        float sy = y + CARD_H - 18;
        int status = Render.mix(Render.mix(Ui.OFF, Ui.ON, on), 0xFFFFFFFF, statusHover * 0.12f);
        Render.roundedRect(x, sy, w, 18, 5, status);
        Render.rect(x, sy, w, 6, status);
        Fonts.MEDIUM.drawCentered(module.isEnabled() ? "ENABLED" : "DISABLED", x + w / 2f, Fonts.MEDIUM.centerY(sy, 18), Ui.TEXT);
    }

    // ---- waypoints tab

    private void drawWaypointBar(int mouseX, int mouseY, float y) {
        int accent = NovaClient.accent();
        float x = mainX();
        boolean inWorld = mc.thePlayer != null;
        float h = hovers.get("addwp", inWorld && Ui.isInside(mouseX, mouseY, x, y, 140, TOOLBAR_H));
        Render.roundedRect(x, y, 140, TOOLBAR_H, 4, inWorld ? Render.mix(accent, 0xFFFFFFFF, h * 0.15f) : Ui.SURFACE);
        Fonts.MEDIUM.drawCentered("+ ADD WAYPOINT HERE", x + 70, Fonts.MEDIUM.centerY(y, TOOLBAR_H), inWorld ? Ui.TEXT : Ui.TEXT_DIM);

        String where = inWorld ? ServerUtil.currentServer() : "all servers";
        if (ServerUtil.isHypixel()) where += "  (hidden on Hypixel)";
        Fonts.REGULAR.draw(where, x + 150, Fonts.REGULAR.centerY(y, TOOLBAR_H), Ui.TEXT_DIM);
    }

    private List<Waypoint> waypointRows() {
        return WaypointManager.forCurrentServer();
    }

    private void drawWaypoints(int mouseX, int mouseY, float offset) {
        List<Waypoint> list = waypointRows();
        maxScroll = Math.max(0, list.size() * (ROW_H + 4) - contentH());
        if (list.isEmpty()) {
            Fonts.REGULAR.drawCentered("No waypoints yet. Click + ADD WAYPOINT HERE to mark where you're standing.",
                    contentX() + contentW() / 2f, contentY() + 30, Ui.TEXT_DIM);
            return;
        }
        float w = contentW();
        for (int i = 0; i < list.size(); i++) {
            Waypoint wp = list.get(i);
            float appear = Anim.cascade(revealStart, i);
            float x = contentX();
            float y = contentY() + i * (ROW_H + 4) - offset + (1f - appear) * 8f;
            float hover = hovers.get(wp, Ui.isInside(mouseX, mouseY, x, y, w, ROW_H));
            Render.roundedRect(x, y, w, ROW_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover));

            Render.circle(x + 11, y + ROW_H / 2f, 4.5f, wp.getColor());
            float textY = Fonts.REGULAR.centerY(y, ROW_H);
            if (editing == wp) {
                float end = Fonts.REGULAR.draw(editText, x + 22, textY, Ui.TEXT);
                drawCaret(end, y + 5, ROW_H - 10);
            } else {
                Fonts.REGULAR.draw(wp.name, x + 22, textY, Ui.TEXT);
            }

            String coords = wp.x + ", " + wp.y + ", " + wp.z;
            if (mc.thePlayer != null && wp.dimension == mc.thePlayer.dimension) {
                coords += "   " + Math.round(Math.sqrt(mc.thePlayer.getDistanceSq(wp.x + 0.5, wp.y, wp.z + 0.5))) + "m";
            } else {
                coords += "   " + dimensionName(wp.dimension);
            }
            Fonts.REGULAR.draw(coords, x + w * 0.42f, textY, Ui.TEXT_DIM);

            // visible switch and delete button
            float sx = x + w - 66;
            float sy = y + (ROW_H - 12) / 2f;
            float shown = states.state(wp.hashCode() + "#vis", wp.visible);
            Render.roundedRect(sx, sy, 36, 12, 2, Render.mix(Ui.OFF, Ui.ON, shown));
            Render.roundedRect(sx + 1 + shown * 18, sy + 1, 17, 10, 1.5f, 0xFF4A4A55);
            Fonts.SMALL.drawCentered("ON", sx + 9, Fonts.SMALL.centerY(sy, 12), Render.alpha(Ui.TEXT, shown));
            Fonts.SMALL.drawCentered("OFF", sx + 27, Fonts.SMALL.centerY(sy, 12), Render.alpha(Ui.TEXT, 1f - shown));

            float dx = x + w - 22;
            float delHover = hovers.get(wp.hashCode() + "#del", Ui.isInside(mouseX, mouseY, dx, y + 3, 16, 16));
            Render.roundedRect(dx, y + 3, 16, 16, 3, Render.mix(Ui.SURFACE_DARK, Ui.OFF, delHover));
            Fonts.MEDIUM.drawCentered("x", dx + 8, Fonts.MEDIUM.centerY(y + 2, 16), Ui.TEXT);

            if (appear < 1f) Render.roundedRect(x, y, w, ROW_H, 4, Render.alpha(Ui.PANEL_SOLID, 1f - appear));
        }
    }

    private static String dimensionName(int dimension) {
        if (dimension == -1) return "Nether";
        if (dimension == 1) return "End";
        return "Overworld";
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
        layout();
        mouseX = mapX(mouseX);
        mouseY = mapY(mouseY);
        searchFocused = false;
        if (editing != null) finishEditing(true);

        if (clickHeader(mouseX, mouseY)) return;
        if (clickSidebar(mouseX, mouseY, button)) return;
        if (clickToolbar(mouseX, mouseY)) return;
        if (!inContent(mouseX, mouseY)) return;

        float offset = scroll.get();
        if (tab == Tab.SETTINGS) {
            if (settingsList.mouseClicked(NovaClient.getSettings().getSettings(), contentX(), contentY() - offset, contentW(), mouseX, mouseY, button)) {
                NovaClient.saveConfig();
            }
            return;
        }
        if (tab == Tab.WAYPOINTS) {
            clickWaypoints(mouseX, mouseY, offset);
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
        float tabX = tabsX();
        for (Tab t : Tab.values()) {
            if (Ui.isInside(mouseX, mouseY, tabX, py + 10, TAB_W, 18)) {
                if (tab != t) {
                    tab = t;
                    reveal();
                }
                return true;
            }
            tabX += TAB_W + 6;
        }
        if (Ui.isInside(mouseX, mouseY, px + pw - 28, py + 10, 18, 18)) {
            close();
            return true;
        }
        return false;
    }

    private boolean clickSidebar(int mouseX, int mouseY, int button) {
        float x = sidebarX();
        float y = sidebarY();
        if (!Ui.isInside(mouseX, mouseY, x, y, SIDEBAR_W, sidebarH())) return false;
        Config config = NovaClient.getConfig();

        if (Ui.isInside(mouseX, mouseY, x + 4, y + sidebarH() - 20, SIDEBAR_W - 8, 16)) {
            mc.displayGuiScreen(new HudEditorScreen());
            return true;
        }
        if (Ui.isInside(mouseX, mouseY, x + 4, y + sidebarH() - 40, SIDEBAR_W - 8, 16)) {
            config.createProfile(NovaClient.getModuleManager(), NovaClient.getSettings());
            return true;
        }

        float rowY = y + 16;
        for (String name : config.listProfiles()) {
            if (rowY + PROFILE_H > y + sidebarH() - 44) break;
            if (Ui.isInside(mouseX, mouseY, x + 4, rowY, SIDEBAR_W - 8, PROFILE_H)) {
                boolean active = name.equals(config.getActiveProfile());
                boolean onDelete = mouseX >= x + SIDEBAR_W - 20;
                if (button == 1 && !name.equals(Config.DEFAULT_PROFILE)) {
                    editing = name;
                    editText = name;
                } else if (onDelete && !active && !name.equals(Config.DEFAULT_PROFILE)) {
                    config.deleteProfile(name);
                } else if (!active) {
                    config.switchTo(name, NovaClient.getModuleManager(), NovaClient.getSettings());
                    page = null;
                    reveal();
                }
                return true;
            }
            rowY += PROFILE_H + 3;
        }
        return true;
    }

    private boolean clickToolbar(int mouseX, int mouseY) {
        float y = toolbarY();
        if (!Ui.isInside(mouseX, mouseY, mainX(), y, mainW(), TOOLBAR_H)) return false;

        if (tab == Tab.WAYPOINTS) {
            if (mc.thePlayer != null && Ui.isInside(mouseX, mouseY, mainX(), y, 140, TOOLBAR_H)) {
                Waypoint added = WaypointManager.addHere();
                if (added != null) {
                    editing = added;
                    editText = added.name;
                }
            }
            return true;
        }
        if (tab != Tab.MODS) return true;
        if (page != null) {
            if (Ui.isInside(mouseX, mouseY, mainX(), y, 52, TOOLBAR_H)) {
                page = null;
                reveal();
            } else if (page instanceof HudModule && Ui.isInside(mouseX, mouseY, mainX() + mainW() - 100, y, 100, TOOLBAR_H)) {
                ((HudModule) page).resetPosition();
                pageSize.set(1);
                NovaClient.saveConfig();
            }
            return true;
        }

        float x = mainX();
        for (String f : FILTERS) {
            float w = Fonts.MEDIUM.getWidth(f) + 14;
            if (Ui.isInside(mouseX, mouseY, x, y, w, TOOLBAR_H)) {
                if (!filter.equals(f)) {
                    filter = f;
                    reveal();
                }
                return true;
            }
            x += w + 4;
        }
        searchFocused = true;
        return true;
    }

    private void clickWaypoints(int mouseX, int mouseY, float offset) {
        List<Waypoint> list = waypointRows();
        float w = contentW();
        for (int i = 0; i < list.size(); i++) {
            Waypoint wp = list.get(i);
            float x = contentX();
            float y = contentY() + i * (ROW_H + 4) - offset;
            if (!Ui.isInside(mouseX, mouseY, x, y, w, ROW_H)) continue;

            if (Ui.isInside(mouseX, mouseY, x + w - 22, y + 3, 16, 16)) {
                WaypointManager.remove(wp);
            } else if (Ui.isInside(mouseX, mouseY, x + w - 66, y, 36, ROW_H)) {
                wp.visible = !wp.visible;
                WaypointManager.save();
            } else if (mouseX < x + 18) {
                wp.nextColor();
                WaypointManager.save();
            } else {
                editing = wp;
                editText = wp.name;
            }
            return;
        }
    }

    /** Ends renaming a profile or waypoint, keeping the new name if save is true and it's valid. */
    private void finishEditing(boolean save) {
        Object target = editing;
        editing = null;
        String text = editText.trim();
        if (!save || text.isEmpty()) return;
        if (target instanceof Waypoint) {
            ((Waypoint) target).name = text;
            WaypointManager.save();
        } else if (target instanceof String && !text.equals(target)) {
            NovaClient.getConfig().renameProfile((String) target, text);
        }
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
        if (editing != null) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) finishEditing(true);
            else if (keyCode == Keyboard.KEY_ESCAPE) finishEditing(false);
            else if (keyCode == Keyboard.KEY_BACK) {
                if (!editText.isEmpty()) editText = editText.substring(0, editText.length() - 1);
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && editText.length() < 16) {
                editText += typedChar;
            }
            return;
        }
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
