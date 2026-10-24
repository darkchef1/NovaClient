package com.nova.gui;

import com.nova.NovaClient;
import com.nova.cosmetic.CapeArt;
import com.nova.cosmetic.Cosmetic;
import com.nova.cosmetic.CosmeticSlot;
import com.nova.cosmetic.Cosmetics;
import com.nova.font.Fonts;
import com.nova.util.Anim;
import com.nova.util.Render;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;

/**
 * The COSMETICS tab: a 3D preview of you wearing your cosmetics on the left (drag to spin),
 * and cards to put them on or take them off on the right.
 */
final class CosmeticsPage {
    private static final int PREVIEW_W = 124;
    private static final int CARD_H = 80;
    private static final int CARD_MIN_W = 84;
    private static final int GAP = 8;
    private static final int TOOLBAR_H = 18;
    private static final int FOLDER_W = 118;

    private final Ui.Hovers hovers = new Ui.Hovers();
    private final Ui.Hovers states = new Ui.Hovers();
    /** The preview turns to show your back for capes and wings. */
    private final Anim yaw = new Anim(5f, 180f);
    private CosmeticSlot slot = CosmeticSlot.CAPE;
    private float spin;
    private boolean dragging;
    private int lastX;
    private long revealStart = System.currentTimeMillis();

    void onOpen() {
        Cosmetics.scanCustomCapes();
        CapeArt.clearCustom();
        revealStart = System.currentTimeMillis();
    }

    private float baseYaw() {
        return slot == CosmeticSlot.CAPE || slot == CosmeticSlot.WINGS ? 160f : 20f;
    }

    // ------------------------------------------------------------------ toolbar

    void drawToolbar(float x, float y, int mouseX, int mouseY) {
        int accent = NovaClient.accent();
        for (CosmeticSlot s : CosmeticSlot.values()) {
            String label = s.displayName.toUpperCase();
            float cw = Fonts.MEDIUM.getWidth(label) + 16;
            float hover = hovers.get("slot" + s, Ui.isInside(mouseX, mouseY, x, y, cw, TOOLBAR_H));
            float active = states.state("slot" + s, slot == s);
            Render.roundedRect(x, y, cw, TOOLBAR_H, 4, Render.mix(Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover), accent, active));
            Fonts.MEDIUM.drawCentered(label, x + cw / 2f, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(hover, active)));
            x += cw + 4;
        }
    }

    /** Where the slot chips end, so the folder button never covers them. */
    private static float chipsEnd(float x) {
        for (CosmeticSlot s : CosmeticSlot.values()) x += Fonts.MEDIUM.getWidth(s.displayName.toUpperCase()) + 16 + 4;
        return x;
    }

    /** The folder button's label and width: shorter on narrow windows. */
    private static String folderLabel(float x, float right) {
        return right - chipsEnd(x) - 6 >= FOLDER_W ? "MY CAPES FOLDER" : "FOLDER";
    }

    private static float folderWidth(String label) {
        return label.length() > 6 ? FOLDER_W : Fonts.MEDIUM.getWidth(label) + 16;
    }

    void drawFolderButton(float x, float right, float y, int mouseX, int mouseY) {
        if (slot != CosmeticSlot.CAPE) return;
        String label = folderLabel(x, right);
        float fw = folderWidth(label);
        float fx = right - fw;
        float hover = hovers.get("folder", Ui.isInside(mouseX, mouseY, fx, y, fw, TOOLBAR_H));
        Render.roundedRect(fx, y, fw, TOOLBAR_H, 4, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover));
        Fonts.MEDIUM.drawCentered(label, fx + fw / 2f, Fonts.MEDIUM.centerY(y, TOOLBAR_H), Ui.TEXT);
    }

    /** @return true if the click hit the toolbar. */
    boolean clickToolbar(float x, float y, float right, int mouseX, int mouseY) {
        float folderW = folderWidth(folderLabel(x, right));
        for (CosmeticSlot s : CosmeticSlot.values()) {
            float cw = Fonts.MEDIUM.getWidth(s.displayName.toUpperCase()) + 16;
            if (Ui.isInside(mouseX, mouseY, x, y, cw, TOOLBAR_H)) {
                if (slot != s) {
                    slot = s;
                    spin = 0;
                    revealStart = System.currentTimeMillis();
                }
                return true;
            }
            x += cw + 4;
        }
        if (slot == CosmeticSlot.CAPE && Ui.isInside(mouseX, mouseY, right - folderW, y, folderW, TOOLBAR_H)) {
            openCapesFolder();
            return true;
        }
        return false;
    }

    private static void openCapesFolder() {
        Thread opener = new Thread(() -> {
            try {
                Desktop.getDesktop().open(Cosmetics.getCapesFolder());
            } catch (Exception ignored) {
            }
        }, "Nova open folder");
        opener.setDaemon(true);
        opener.start();
    }

    // ------------------------------------------------------------------ content

    private List<Cosmetic> items() {
        List<Cosmetic> list = new ArrayList<>();
        list.add(null); // "None" card
        list.addAll(Cosmetics.catalog(slot));
        return list;
    }

    private int columns(float gridW) {
        return Math.max(2, (int) ((gridW + GAP) / (CARD_MIN_W + GAP)));
    }

    /** Draws the page. @return how far it can scroll. */
    float draw(float x, float y, float w, float h, float offset, int mouseX, int mouseY) {
        // the 3D preview stays still while the cards scroll
        float px = x, pw = PREVIEW_W;
        Render.roundedRect(px, y, pw, h, 6, Ui.SURFACE_DARK);
        Render.glow(px + pw / 2f, y + h * 0.55f, pw * 0.7f, NovaClient.accent(), 0.4f);
        Fonts.SMALL.drawCentered("Drag to spin", px + pw / 2f, y + 5, Ui.TEXT_DIM);
        if (!dragging) yaw.setTarget(baseYaw() + spin);
        float size = Math.min(50f, (h - 26) / 1.95f);
        float feetY = y + h - 10;
        float cx = px + pw / 2f;
        PlayerPreview.draw(cx, feetY, size, yaw.get(), mouseX - cx, mouseY - (feetY - size * 1.62f), Cosmetics.getEquipped());

        float gx = x + pw + 10, gw = w - pw - 10;
        List<Cosmetic> list = items();
        int cols = columns(gw);
        float cw = (gw - (cols - 1) * GAP) / cols;
        int rows = (list.size() + cols - 1) / cols;
        for (int i = 0; i < list.size(); i++) {
            float cardX = gx + (i % cols) * (cw + GAP);
            float cardY = y + (i / cols) * (CARD_H + GAP) - offset;
            if (cardY + CARD_H < y - 2 || cardY > y + h + 2) continue;
            float appear = Anim.cascade(revealStart, i);
            float slide = (1f - appear) * 10f;
            drawCard(list.get(i), cardX, cardY + slide, cw, appear < 1f ? -1 : mouseX, mouseY);
            if (appear < 1f) Render.roundedRect(cardX, cardY + slide - 2, cw, CARD_H + 4, 5, Render.alpha(Ui.PANEL_SOLID, 1f - appear));
        }
        if (slot == CosmeticSlot.CAPE && Cosmetics.catalog(slot).size() == 8 && rows * (CARD_H + GAP) < h - 20) {
            Fonts.SMALL.draw("Tip: put PNG images in MY CAPES FOLDER to wear your own capes.", gx, y + rows * (CARD_H + GAP) + 2, Ui.TEXT_DIM);
        }
        return Math.max(0, rows * (CARD_H + GAP) - GAP - h);
    }

    private void drawCard(Cosmetic cosmetic, float x, float y, float w, int mouseX, int mouseY) {
        String id = cosmetic == null ? null : cosmetic.id;
        String current = Cosmetics.getEquipped(slot);
        boolean worn = id == null ? current == null : id.equals(current);
        int accent = NovaClient.accent();
        float hover = hovers.get(slot + ":" + id, Ui.isInside(mouseX, mouseY, x, y, w, CARD_H));
        float on = states.state(slot + ":" + id, worn);

        float lift = hover * 2f;
        Render.roundedRect(x + 1, y + 3 - lift, w - 2, CARD_H, 5, Render.alpha(0x60000000, hover));
        y -= lift;
        Render.roundedRect(x, y, w, CARD_H, 5, Render.mix(Ui.SURFACE, Ui.SURFACE_HOVER, hover));
        if (on > 0.01f) Render.roundedOutline(x, y, w, CARD_H, 5, 0.75f, Render.alpha(accent, on));

        // picture
        float picY = y + 6, picH = 40;
        if (cosmetic == null) {
            Render.arc(x + w / 2f, picY + picH / 2f, 11, 2f, 0, 360, Ui.TEXT_DIM);
            Render.rect(x + w / 2f - 7, picY + picH / 2f - 1, 14, 2, Ui.TEXT_DIM);
        } else if (slot == CosmeticSlot.CAPE) {
            float th = picH, tw = th * 10f / 16f;
            Render.roundedRect(x + w / 2f - tw / 2f - 1, picY - 1, tw + 2, th + 2, 2, 0x40000000);
            Render.texture(CapeArt.texture(id), x + w / 2f - tw / 2f, picY, tw, th, 1 / 64f, 1 / 32f, 11 / 64f, 17 / 32f);
        } else {
            Render.item(icon(id), x + w / 2f - 14 - hover, picY + 4 - hover, 1.75f + hover * 0.1f);
        }
        if (cosmetic != null && cosmetic.animated && slot == CosmeticSlot.CAPE) {
            Fonts.SMALL.draw("ANIMATED", x + 5, y + 4, Render.alpha(accent, 0.9f));
        }

        Fonts.MEDIUM.drawCentered(cosmetic == null ? "None" : cosmetic.name, x + w / 2f, y + 50, Render.mix(Ui.TEXT_DIM, Ui.TEXT, Math.max(on, hover)));

        float by = y + CARD_H - 16;
        int bar = Render.mix(Render.mix(Ui.SURFACE_HOVER, 0xFF2E2E3A, hover), accent, on);
        Render.roundedRect(x, by, w, 16, 5, bar);
        Render.rect(x, by, w, 6, bar);
        Fonts.SMALL.drawCentered(worn ? (cosmetic == null ? "SELECTED" : "EQUIPPED") : (cosmetic == null ? "REMOVE" : "EQUIP"),
                x + w / 2f, Fonts.SMALL.centerY(by, 16), Ui.TEXT);
    }

    private static ItemStack icon(String id) {
        switch (id) {
            case "angel": return new ItemStack(Items.feather);
            case "dragon": return new ItemStack(Blocks.dragon_egg);
            case "crystal": return new ItemStack(Items.prismarine_crystals);
            case "phoenix": return new ItemStack(Items.blaze_powder);
            case "demon": return new ItemStack(Items.ender_eye);
            case "butterfly": return new ItemStack(Blocks.red_flower, 1, 2);
            case "fairy": return new ItemStack(Items.glowstone_dust);
            case "bat": return new ItemStack(Items.leather);
            case "halo": return new ItemStack(Items.gold_nugget);
            case "rainbow_halo": return new ItemStack(Items.nether_star);
            case "crown": return new ItemStack(Items.golden_helmet);
            case "horns": return new ItemStack(Items.fire_charge);
            case "cat_ears": return new ItemStack(Items.fish);
            case "bunny_ears": return new ItemStack(Items.carrot);
            case "top_hat": return new ItemStack(Items.leather_helmet);
            case "flower_crown": return new ItemStack(Blocks.red_flower, 1, 7);
            case "rainbow_stars": return new ItemStack(Items.fireworks);
            case "hearts": return new ItemStack(Items.cake);
            case "embers": return new ItemStack(Items.magma_cream);
            case "snow": return new ItemStack(Items.snowball);
            case "atom": return new ItemStack(Items.ender_pearl);
            default: return new ItemStack(Items.nether_star);
        }
    }

    /** @return true if a cosmetic was put on or taken off. */
    boolean click(float x, float y, float w, float h, float offset, int mouseX, int mouseY) {
        if (Ui.isInside(mouseX, mouseY, x, y, PREVIEW_W, h)) {
            dragging = true;
            lastX = mouseX;
            return false;
        }
        float gx = x + PREVIEW_W + 10, gw = w - PREVIEW_W - 10;
        List<Cosmetic> list = items();
        int cols = columns(gw);
        float cw = (gw - (cols - 1) * GAP) / cols;
        for (int i = 0; i < list.size(); i++) {
            float cardX = gx + (i % cols) * (cw + GAP);
            float cardY = y + (i / cols) * (CARD_H + GAP) - offset;
            if (!Ui.isInside(mouseX, mouseY, cardX, cardY, cw, CARD_H)) continue;
            Cosmetic c = list.get(i);
            String current = Cosmetics.getEquipped(slot);
            if (c == null || c.id.equals(current)) Cosmetics.equip(slot, null);
            else Cosmetics.equip(slot, c.id);
            return true;
        }
        return false;
    }

    void drag(int mouseX) {
        if (!dragging) return;
        spin += (mouseX - lastX) * 2.5f;
        lastX = mouseX;
        yaw.snap(baseYaw() + spin);
    }

    void release() {
        dragging = false;
    }
}
