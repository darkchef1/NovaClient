package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.NumberSetting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * More hit particles: crit stars on every hit, and more sharpness sparkles.
 * The particles are only on your screen; your hits are not changed.
 */
public class Particles extends Module {
    private final NumberSetting multiplier = add(new NumberSetting("Amount", 2, 1, 5, 1, "x"));
    private final BooleanSetting alwaysCrits = add(new BooleanSetting("Crit particles on every hit", true));
    private final BooleanSetting alwaysSharpness = add(new BooleanSetting("Sharpness particles on every hit", false));

    public Particles() {
        super("Particles", "More hit particles (crits and sharpness)", Category.PVP);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.glowstone_dust);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!isEnabled() || event.entityPlayer != mc.thePlayer) return;
        Entity target = event.target;
        boolean sharp = alwaysSharpness.isOn();
        if (!sharp && target instanceof EntityLivingBase && mc.thePlayer.getHeldItem() != null) {
            sharp = EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), ((EntityLivingBase) target).getCreatureAttribute()) > 0;
        }
        for (int i = 0; i < multiplier.getInt(); i++) {
            if (alwaysCrits.isOn()) mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CRIT);
            if (sharp) mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CRIT_MAGIC);
        }
    }
}
