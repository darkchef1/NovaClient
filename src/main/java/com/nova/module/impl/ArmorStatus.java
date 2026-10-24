package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import com.nova.util.Render;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Armor and held item with durability, Lunar style: on the right side of the screen the
 * numbers sit to the left of the icons, on the left side they sit to the right.
 */
public class ArmorStatus extends HudModule {
    private static final int ROW = 18;
    private static final int ICON = 16;
    private static final int PAD = 3;

    private final BooleanSetting showHeldItem = add(new BooleanSetting("Show held item", true));
    private final BooleanSetting background = add(new BooleanSetting("Background", true));
    private final ModeSetting numberColor = add(new ModeSetting("Number color", "White", "White", "By durability"));

    public ArmorStatus() {
        super("Armor Status", "Shows your armor and its durability", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.END, 2, 2);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.diamond_chestplate);
    }

    private List<ItemStack> getItems(boolean preview) {
        List<ItemStack> items = new ArrayList<>();
        if (mc.thePlayer != null) {
            if (showHeldItem.isOn() && mc.thePlayer.getHeldItem() != null) items.add(mc.thePlayer.getHeldItem());
            for (int slot = 3; slot >= 0; slot--) {
                ItemStack armor = mc.thePlayer.inventory.armorInventory[slot];
                if (armor != null) items.add(armor);
            }
        }
        if (items.isEmpty() && preview) {
            items.add(new ItemStack(Items.diamond_sword));
            items.add(new ItemStack(Items.diamond_helmet));
            items.add(new ItemStack(Items.diamond_chestplate));
            items.add(new ItemStack(Items.diamond_leggings));
            items.add(new ItemStack(Items.diamond_boots));
        }
        return items;
    }

    private static String amountText(ItemStack stack) {
        if (stack.isItemStackDamageable()) return Integer.toString(stack.getMaxDamage() - stack.getItemDamage());
        if (stack.stackSize > 1) return Integer.toString(stack.stackSize);
        return "";
    }

    private int textWidth(List<ItemStack> items) {
        int max = 0;
        for (ItemStack stack : items) max = Math.max(max, mc.fontRendererObj.getStringWidth(amountText(stack)));
        return max;
    }

    @Override
    public int getWidth() {
        return PAD * 2 + ICON + 4 + Math.max(18, textWidth(getItems(true)));
    }

    @Override
    public int getHeight() {
        return PAD * 2 + ROW * Math.max(1, getItems(true).size()) - 2;
    }

    @Override
    public void render(boolean preview) {
        List<ItemStack> items = getItems(preview);
        if (items.isEmpty()) return;
        int width = getWidth();
        boolean numbersLeft = getAnchorX() == Anchor.END;
        if (background.isOn()) Gui.drawRect(0, 0, width, getHeight(), 0x6F000000);

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int y = PAD + i * ROW;
            int iconX = numbersLeft ? width - PAD - ICON : PAD;
            Render.item(stack, iconX, y, 1f);

            String text = amountText(stack);
            if (text.isEmpty()) continue;
            int textX = numbersLeft ? iconX - 4 - mc.fontRendererObj.getStringWidth(text) : iconX + ICON + 4;
            mc.fontRendererObj.drawStringWithShadow(text, textX, y + 4, colorFor(stack));
        }
    }

    private int colorFor(ItemStack stack) {
        if (numberColor.is("White") || !stack.isItemStackDamageable()) return 0xFFFFFFFF;
        float left = (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage();
        return left > 0.5f ? 0xFF55FF55 : left > 0.2f ? 0xFFFFFF55 : 0xFFFF5555;
    }
}
