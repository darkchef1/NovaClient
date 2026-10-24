package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Counts up from when you turn it on. Give it a toggle key to start and stop it quickly. */
public class Stopwatch extends TextHudModule {
    private long startedAt;

    public Stopwatch() {
        super("Stopwatch", "Counts time from when you turn it on", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.CENTER, Anchor.START, 0, 40);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.repeater);
    }

    @Override
    public void onEnable() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    protected String getText() {
        return format(System.currentTimeMillis() - startedAt);
    }

    @Override
    protected String getPreviewText() {
        return "01:23.4";
    }

    private static String format(long ms) {
        long minutes = ms / 60_000;
        long seconds = ms / 1000 % 60;
        long tenths = ms / 100 % 10;
        return String.format("%02d:%02d.%d", minutes, seconds, tenths);
    }
}
