package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import net.minecraft.client.Minecraft;

public class FpsDisplay extends TextHudModule {
    public FpsDisplay() {
        super("FPS", "Shows your frames per second", Category.HUD, 62, 22);
    }

    @Override
    protected String getText() {
        return Minecraft.getDebugFPS() + " FPS";
    }

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.redstone);
    }
}
