package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ServerAddress extends TextHudModule {
    public ServerAddress() {
        super("Server Address", "Shows which server you're on", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.CENTER, Anchor.START, 0, 2);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.command_block);
    }

    @Override
    protected String getText() {
        if (mc.isSingleplayer()) return "Singleplayer";
        return mc.getCurrentServerData() == null ? null : mc.getCurrentServerData().serverIP;
    }

    @Override
    protected String getPreviewText() {
        return "hypixel.net";
    }
}
