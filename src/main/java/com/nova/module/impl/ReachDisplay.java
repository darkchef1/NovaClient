package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.util.CombatTracker;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** How far away your last hit was. Display only; it doesn't change your reach. */
public class ReachDisplay extends TextHudModule {
    public ReachDisplay() {
        super("Reach Display", "Shows the distance of your last hit", Category.PVP, 2, 2);
        setDefaultPosition(Anchor.START, Anchor.CENTER, 2, 20);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.stick);
    }

    @Override
    protected String getText() {
        double reach = CombatTracker.getLastReach(3000);
        return reach < 0 ? "Hasn't attacked" : String.format("%.2f blocks", reach);
    }

    @Override
    protected String getPreviewText() {
        return "3.00 blocks";
    }
}
