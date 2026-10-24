package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/** Shows the boxes Minecraft uses for hits (the same as F3 + B), as a mod you can bind to a key. */
public class Hitboxes extends Module {
    public Hitboxes() {
        super("Hitboxes", "Show hitboxes (like F3 + B)", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.web);
    }

    @Override
    public void onTick() {
        if (!mc.getRenderManager().isDebugBoundingBox()) mc.getRenderManager().setDebugBoundingBox(true);
    }

    @Override
    public void onDisable() {
        mc.getRenderManager().setDebugBoundingBox(false);
    }
}
