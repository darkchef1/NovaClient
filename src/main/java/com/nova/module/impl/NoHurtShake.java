package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Turns off the screen shake when you take damage. Applied by MixinEntityRenderer. */
public class NoHurtShake extends Module {
    public NoHurtShake() {
        super("No Hurt Shake", "No screen shake when you take damage", Category.PVP);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.iron_chestplate);
    }
}
