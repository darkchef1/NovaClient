package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.util.CombatTracker;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Hits in a row without getting hit back. Resets when you take damage or stop hitting for 2 seconds. */
public class ComboCounter extends TextHudModule {
    public ComboCounter() {
        super("Combo Counter", "Shows how many hits in a row you've landed", Category.PVP, 2, 2);
        setDefaultPosition(Anchor.START, Anchor.CENTER, 2, 0);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.blaze_powder);
    }

    @Override
    protected String getText() {
        int combo = CombatTracker.getCombo();
        return combo == 0 ? "No Combo" : combo + " Combo";
    }

    @Override
    protected String getPreviewText() {
        return "3 Combo";
    }
}
