package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorStatus extends HudModule {
    private static final int ROW = 16;
    private static final int WIDTH = 50;

    private final BooleanSetting showHeldItem = add(new BooleanSetting("Show held item", true));

    public ArmorStatus() {
        super("Armor Status", "Shows your armor and its durability", Category.HUD, 2, 190);
    }

    private List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        if (mc.thePlayer == null) return items;
        for (int slot = 3; slot >= 0; slot--) {
            ItemStack armor = mc.thePlayer.inventory.armorInventory[slot];
            if (armor != null) items.add(armor);
        }
        if (showHeldItem.isOn() && mc.thePlayer.getHeldItem() != null) {
            items.add(mc.thePlayer.getHeldItem());
        }
        return items;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return ROW * Math.max(1, getItems().size());
    }

    @Override
    public void render(boolean preview) {
        List<ItemStack> items = getItems();
        if (items.isEmpty()) {
            if (preview) mc.fontRendererObj.drawStringWithShadow("Armor", 2, 4, 0xFFFFFFFF);
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int y = i * ROW;
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, y);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();

            String text = null;
            int color = 0xFFFFFFFF;
            if (stack.isItemStackDamageable()) {
                int left = stack.getMaxDamage() - stack.getItemDamage();
                text = Integer.toString(left);
                float fraction = left / (float) stack.getMaxDamage();
                color = fraction > 0.5f ? 0xFF55FF55 : fraction > 0.2f ? 0xFFFFFF55 : 0xFFFF5555;
            } else if (stack.stackSize > 1) {
                text = Integer.toString(stack.stackSize);
            }
            if (text != null) mc.fontRendererObj.drawStringWithShadow(text, 19, y + 4, color);
        }
    }

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.diamond_chestplate);
    }
}
