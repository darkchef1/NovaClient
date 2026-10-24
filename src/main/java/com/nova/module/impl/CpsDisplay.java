package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.ModeSetting;
import com.nova.util.CpsTracker;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CpsDisplay extends TextHudModule {
    private final ModeSetting show = add(new ModeSetting("Show", "Left + Right", "Left + Right", "Left only"));

    public CpsDisplay() {
        super("CPS", "Shows your clicks per second", Category.HUD, 2, 20);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.stone_sword);
    }

    @Override
    protected String getText() {
        if (show.is("Left only")) return CpsTracker.getLeftCps() + " CPS";
        return CpsTracker.getLeftCps() + " | " + CpsTracker.getRightCps() + " CPS";
    }
}
