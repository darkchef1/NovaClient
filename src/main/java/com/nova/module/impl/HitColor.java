package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.Render;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Changes the red flash players and mobs get when hit. Applied by MixinRendererLivingEntity. */
public class HitColor extends Module {
    private final ModeSetting color = add(new ModeSetting("Color", "Pink",
            "Red", "Pink", "Purple", "Blue", "Aqua", "Green", "Yellow", "Orange", "White", "Accent", "Chroma"));
    private final NumberSetting opacity = add(new NumberSetting("Opacity", 0.35, 0.1, 0.8, 0.05));

    public HitColor() {
        super("Hit Color", "Change the color players flash when hit", Category.PVP);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.dye, 1, 9);
    }

    /** The hit color as {red, green, blue, alpha}, each 0 to 1. */
    public float[] getColor() {
        int rgb;
        switch (color.getMode()) {
            case "Red": rgb = 0xFF0000; break;
            case "Purple": rgb = 0xA040FF; break;
            case "Blue": rgb = 0x3060FF; break;
            case "Aqua": rgb = 0x30E0FF; break;
            case "Green": rgb = 0x30FF60; break;
            case "Yellow": rgb = 0xFFE030; break;
            case "Orange": rgb = 0xFF8020; break;
            case "White": rgb = 0xFFFFFF; break;
            case "Accent": rgb = NovaClient.accent(); break;
            case "Chroma": rgb = Render.chroma(0); break;
            default: rgb = 0xFF40B0;
        }
        return new float[]{((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, opacity.getFloat()};
    }
}
