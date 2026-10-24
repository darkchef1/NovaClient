package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** How much of the memory Minecraft is allowed to use is in use right now. */
public class MemoryUsage extends TextHudModule {
    private final BooleanSetting showMegabytes = add(new BooleanSetting("Show MB", false));

    public MemoryUsage() {
        super("Memory Usage", "Shows how much memory the game uses", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.START, 2, 40);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.comparator);
    }

    @Override
    protected String getText() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);
        int percent = (int) (used * 100 / Math.max(1, max));
        String color = percent < 60 ? GREEN : percent < 85 ? YELLOW : RED;
        String text = "Mem: " + value(color, percent + "%");
        return showMegabytes.isOn() ? text + " " + used + "/" + max + "MB" : text;
    }
}
