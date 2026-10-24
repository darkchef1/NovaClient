package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Clock extends TextHudModule {
    private final BooleanSetting twentyFourHour = add(new BooleanSetting("24-hour time", false));
    private final BooleanSetting seconds = add(new BooleanSetting("Show seconds", false));

    public Clock() {
        super("Clock", "Shows the real-world time", Category.HUD, 2, 42);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.clock);
    }

    @Override
    protected String getText() {
        String pattern = twentyFourHour.isOn() ? "HH:mm" : "h:mm";
        if (seconds.isOn()) pattern += ":ss";
        if (!twentyFourHour.isOn()) pattern += " a";
        return new SimpleDateFormat(pattern).format(new Date());
    }
}
