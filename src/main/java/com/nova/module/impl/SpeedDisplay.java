package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** How fast you're moving, in blocks per second. */
public class SpeedDisplay extends TextHudModule {
    private final BooleanSetting includeVertical = add(new BooleanSetting("Count up and down", false));
    private double speed;

    public SpeedDisplay() {
        super("Speed", "Shows how fast you're moving", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.START, 2, 58);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.diamond_boots);
    }

    @Override
    public void onTick() {
        double dx = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double dz = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        double dy = includeVertical.isOn() ? mc.thePlayer.posY - mc.thePlayer.prevPosY : 0;
        double perTick = Math.sqrt(dx * dx + dy * dy + dz * dz);
        speed += (perTick * 20 - speed) * 0.5; // smooth it so the number doesn't flicker
    }

    @Override
    protected String getText() {
        return String.format("%.2f m/s", speed);
    }

    @Override
    protected String getPreviewText() {
        return "5.61 m/s";
    }
}
