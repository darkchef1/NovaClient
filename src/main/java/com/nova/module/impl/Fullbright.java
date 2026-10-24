package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/** See in the dark without night vision, by turning brightness far past the normal maximum. */
public class Fullbright extends Module {
    private static final float BRIGHT = 100f;
    private float savedGamma = 1f;

    public Fullbright() {
        super("Fullbright", "See in the dark", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.torch);
    }

    @Override
    public void onEnable() {
        float gamma = mc.gameSettings.gammaSetting;
        // if the game was closed while this was on, the saved brightness is ours, not the player's
        savedGamma = gamma > 1f ? 1f : gamma;
        mc.gameSettings.gammaSetting = BRIGHT;
    }

    @Override
    public void onTick() {
        mc.gameSettings.gammaSetting = BRIGHT;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = savedGamma;
    }
}
