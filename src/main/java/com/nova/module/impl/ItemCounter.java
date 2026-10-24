package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import com.nova.util.Render;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/** How many healing pots, soups, pearls, golden apples and arrows you have left. */
public class ItemCounter extends HudModule {
    private static final int ROW = 18;
    private static final int ICON = 16;
    private static final int PAD = 3;

    private final BooleanSetting pots = add(new BooleanSetting("Healing pots", true));
    private final BooleanSetting soups = add(new BooleanSetting("Soups", true));
    private final BooleanSetting pearls = add(new BooleanSetting("Ender pearls", true));
    private final BooleanSetting gapples = add(new BooleanSetting("Golden apples", true));
    private final BooleanSetting arrows = add(new BooleanSetting("Arrows", true));
    private final BooleanSetting hideZero = add(new BooleanSetting("Hide items you have none of", true));
    private final BooleanSetting background = add(new BooleanSetting("Background", true));

    /** One row: the icon to draw and how many you have. */
    private static final class Entry {
        final ItemStack icon;
        final int count;

        Entry(ItemStack icon, int count) {
            this.icon = icon;
            this.count = count;
        }
    }

    public ItemCounter() {
        super("Item Counter", "Shows how many pots, pearls and gapples you have left", Category.PVP, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.CENTER, 2, 0);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.potionitem, 1, 16421);
    }

    private static boolean isHealingSplash(ItemStack stack) {
        if (stack.getItem() != Items.potionitem || !ItemPotion.isSplash(stack.getMetadata())) return false;
        List<PotionEffect> effects = Items.potionitem.getEffects(stack);
        if (effects == null) return false;
        for (PotionEffect effect : effects) {
            if (effect.getPotionID() == Potion.heal.id) return true;
        }
        return false;
    }

    private List<Entry> entries(boolean preview) {
        int potCount = 0, soupCount = 0, pearlCount = 0, gappleCount = 0, arrowCount = 0;
        ItemStack potIcon = new ItemStack(Items.potionitem, 1, 16421);
        if (mc.thePlayer != null) {
            for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
                if (stack == null) continue;
                if (isHealingSplash(stack)) {
                    potCount += stack.stackSize;
                    potIcon = stack;
                } else if (stack.getItem() == Items.mushroom_stew) soupCount += stack.stackSize;
                else if (stack.getItem() == Items.ender_pearl) pearlCount += stack.stackSize;
                else if (stack.getItem() == Items.golden_apple) gappleCount += stack.stackSize;
                else if (stack.getItem() == Items.arrow) arrowCount += stack.stackSize;
            }
        }
        if (preview && potCount + soupCount + pearlCount + gappleCount + arrowCount == 0) {
            potCount = 32;
            pearlCount = 16;
            gappleCount = 2;
        }
        List<Entry> list = new ArrayList<>();
        add(list, pots, potIcon, potCount);
        add(list, soups, new ItemStack(Items.mushroom_stew), soupCount);
        add(list, pearls, new ItemStack(Items.ender_pearl), pearlCount);
        add(list, gapples, new ItemStack(Items.golden_apple), gappleCount);
        add(list, arrows, new ItemStack(Items.arrow), arrowCount);
        return list;
    }

    private void add(List<Entry> list, BooleanSetting shown, ItemStack icon, int count) {
        if (!shown.isOn() || (count == 0 && hideZero.isOn())) return;
        ItemStack single = icon.copy();
        single.stackSize = 1;
        list.add(new Entry(single, count));
    }

    @Override
    public int getWidth() {
        int text = 12;
        for (Entry e : entries(true)) text = Math.max(text, mc.fontRendererObj.getStringWidth(Integer.toString(e.count)));
        return PAD * 2 + ICON + 4 + text;
    }

    @Override
    public int getHeight() {
        return PAD * 2 + ROW * Math.max(1, entries(true).size()) - 2;
    }

    @Override
    public void render(boolean preview) {
        List<Entry> list = entries(preview);
        if (list.isEmpty()) return;
        int width = getWidth();
        boolean numbersLeft = getAnchorX() == Anchor.END;
        if (background.isOn()) Gui.drawRect(0, 0, width, getHeight(), 0x6F000000);
        for (int i = 0; i < list.size(); i++) {
            Entry e = list.get(i);
            int y = PAD + i * ROW;
            int iconX = numbersLeft ? width - PAD - ICON : PAD;
            Render.item(e.icon, iconX, y, 1f);
            String text = Integer.toString(e.count);
            int textX = numbersLeft ? iconX - 4 - mc.fontRendererObj.getStringWidth(text) : iconX + ICON + 4;
            mc.fontRendererObj.drawStringWithShadow(text, textX, y + 4, e.count == 0 ? 0xFFFF5555 : 0xFFFFFFFF);
        }
    }
}
