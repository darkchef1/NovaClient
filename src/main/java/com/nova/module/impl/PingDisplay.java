package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingDisplay extends TextHudModule {
    public PingDisplay() {
        super("Ping", "Shows your ping to the server", Category.HUD, 2, 38);
    }

    @Override
    protected String getText() {
        if (mc.isSingleplayer() || mc.getNetHandler() == null) return "0 ms";
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? null : info.getResponseTime() + " ms";
    }

    @Override
    protected String getPreviewText() {
        return "0 ms";
    }

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.ender_eye);
    }
}
