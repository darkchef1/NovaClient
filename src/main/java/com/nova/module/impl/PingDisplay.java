package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PingDisplay extends TextHudModule {
    private final BooleanSetting colored = add(new BooleanSetting("Color by ping", true));

    public PingDisplay() {
        super("Ping", "Shows your ping to the server", Category.HUD, 2, 2);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.ender_eye);
    }

    @Override
    protected String getText() {
        int ping = 0;
        if (!mc.isSingleplayer() && mc.getNetHandler() != null) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (info == null) return null;
            ping = info.getResponseTime();
        }
        return format(ping);
    }

    @Override
    protected String getPreviewText() {
        return format(59);
    }

    private String format(int ping) {
        if (!colored.isOn()) return "Ping: " + ping + " ms";
        String color = ping < 100 ? GREEN : ping < 150 ? YELLOW : ping < 250 ? GOLD : RED;
        return "Ping: " + value(color, ping + " ms");
    }
}
