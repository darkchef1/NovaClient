package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class DayCounter extends TextHudModule {
    public DayCounter() {
        super("Day Counter", "Shows how many days the world has been running", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.CENTER, Anchor.START, 0, 22);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.bed);
    }

    @Override
    protected String getText() {
        long days = mc.theWorld.getWorldTime() / 24000L;
        return days + (days == 1 ? " Day" : " Days");
    }

    @Override
    protected String getPreviewText() {
        return "32 Days";
    }
}
