package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.util.MathHelper;

public class Coordinates extends TextHudModule {
    private final BooleanSetting showDirection = add(new BooleanSetting("Show direction", true));

    public Coordinates() {
        super("Coordinates", "Shows your X Y Z position", Category.HUD, 2, 56);
    }

    @Override
    protected String getText() {
        String text = "XYZ: " + MathHelper.floor_double(mc.thePlayer.posX)
                + ", " + MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY)
                + ", " + MathHelper.floor_double(mc.thePlayer.posZ);
        if (showDirection.isOn()) {
            String facing = mc.thePlayer.getHorizontalFacing().getName();
            text += " (" + Character.toUpperCase(facing.charAt(0)) + ")";
        }
        return text;
    }

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.compass);
    }
}
