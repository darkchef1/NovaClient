package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** X, Y and Z on separate lines, plus optional facing direction and biome. */
public class Coordinates extends TextHudModule {
    private final BooleanSetting showDirection = add(new BooleanSetting("Show direction", true));
    private final BooleanSetting showBiome = add(new BooleanSetting("Show biome", true));

    public Coordinates() {
        super("Coordinates", "Shows your X Y Z position", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.START, Anchor.END, 2, 2);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.compass);
    }

    @Override
    protected List<String> getLines() {
        List<String> lines = new ArrayList<>();
        lines.add("X: " + MathHelper.floor_double(mc.thePlayer.posX));
        lines.add("Y: " + MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY));
        lines.add("Z: " + MathHelper.floor_double(mc.thePlayer.posZ));
        if (showDirection.isOn()) lines.add("F: " + direction(mc.thePlayer.getHorizontalFacing()));
        if (showBiome.isOn()) {
            BlockPos pos = new BlockPos(mc.thePlayer);
            lines.add("Biome: " + value(GREEN, mc.theWorld.getBiomeGenForCoords(pos).biomeName));
        }
        return lines;
    }

    @Override
    protected List<String> getPreviewLines() {
        return Arrays.asList("X: 0", "Y: 64", "Z: 0", "F: North (-Z)", "Biome: " + value(GREEN, "Plains"));
    }

    private static String direction(EnumFacing facing) {
        switch (facing) {
            case NORTH: return "North (-Z)";
            case SOUTH: return "South (+Z)";
            case WEST: return "West (-X)";
            default: return "East (+X)";
        }
    }
}
