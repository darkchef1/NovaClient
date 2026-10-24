package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.ModeSetting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Changes the time of day on your screen only. The server's time is untouched. */
public class TimeChanger extends Module {
    private final ModeSetting time = add(new ModeSetting("Time", "Day", "Morning", "Day", "Sunset", "Night", "Midnight"));

    public TimeChanger() {
        super("Time Changer", "Pick the time of day, only for you", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.clock);
    }

    @Override
    public void onTick() {
        mc.theWorld.setWorldTime(ticks());
    }

    private long ticks() {
        switch (time.getMode()) {
            case "Morning": return 0;
            case "Sunset": return 12500;
            case "Night": return 14500;
            case "Midnight": return 18000;
            default: return 6000;
        }
    }
}
