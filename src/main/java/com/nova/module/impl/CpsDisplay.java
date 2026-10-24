package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import com.nova.util.CpsTracker;

public class CpsDisplay extends TextHudModule {
    private final BooleanSetting showRight = add(new BooleanSetting("Show right click", false));

    public CpsDisplay() {
        super("CPS", "Shows your clicks per second", Category.HUD, 2, 20);
    }

    @Override
    protected String getText() {
        if (showRight.isOn()) {
            return CpsTracker.getLeftCps() + " | " + CpsTracker.getRightCps() + " CPS";
        }
        return CpsTracker.getLeftCps() + " CPS";
    }

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.stone_sword);
    }
}
