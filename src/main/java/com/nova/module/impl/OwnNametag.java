package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Shows your own name tag in third person (F5). Applied by MixinRendererLivingEntity. */
public class OwnNametag extends Module {
    public OwnNametag() {
        super("Own Nametag", "See your own name tag in F5", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.name_tag);
    }
}
