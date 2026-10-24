package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Active potion effects with icon, name, level and time left, Lunar style. */
public class PotionEffects extends HudModule {
    private static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");
    private static final int ROW = 22;
    private static final int PAD = 3;
    private static final int ICON = 18;

    private final BooleanSetting background = add(new BooleanSetting("Background", true));
    private final BooleanSetting blinkWhenEnding = add(new BooleanSetting("Blink when ending", true));

    public PotionEffects() {
        super("Potion Effects", "Shows your active potion effects", Category.HUD, 2, 140);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.potionitem, 1, 8194);
    }

    private List<PotionEffect> getEffects(boolean preview) {
        List<PotionEffect> effects = new ArrayList<>();
        if (mc.thePlayer != null) effects.addAll(mc.thePlayer.getActivePotionEffects());
        if (effects.isEmpty() && preview) {
            effects.addAll(Arrays.asList(
                    new PotionEffect(Potion.moveSpeed.id, 1200, 1),
                    new PotionEffect(Potion.damageBoost.id, 600, 0)));
        }
        return effects;
    }

    private static String name(PotionEffect effect) {
        String name = I18n.format(Potion.potionTypes[effect.getPotionID()].getName());
        if (effect.getAmplifier() > 0) name += " " + I18n.format("enchantment.level." + (effect.getAmplifier() + 1));
        return name;
    }

    @Override
    public int getWidth() {
        int max = 40;
        for (PotionEffect effect : getEffects(true)) {
            max = Math.max(max, mc.fontRendererObj.getStringWidth(name(effect)));
            max = Math.max(max, mc.fontRendererObj.getStringWidth(Potion.getDurationString(effect)));
        }
        return PAD * 2 + ICON + 4 + max;
    }

    @Override
    public int getHeight() {
        return PAD * 2 + ROW * Math.max(1, getEffects(true).size()) - 4;
    }

    @Override
    public void render(boolean preview) {
        List<PotionEffect> effects = getEffects(preview);
        if (effects.isEmpty()) return;
        if (background.isOn()) Gui.drawRect(0, 0, getWidth(), getHeight(), 0x6F000000);

        for (int i = 0; i < effects.size(); i++) {
            PotionEffect effect = effects.get(i);
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            int y = PAD + i * ROW;

            boolean ending = effect.getDuration() < 200 && !effect.getIsPotionDurationMax();
            boolean hidden = blinkWhenEnding.isOn() && ending && (System.currentTimeMillis() / 400) % 2 == 0;
            if (potion.hasStatusIcon() && !hidden) {
                int index = potion.getStatusIconIndex();
                GlStateManager.color(1f, 1f, 1f, 1f);
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(INVENTORY);
                Gui.drawModalRectWithCustomSizedTexture(PAD, y, index % 8 * 18, 198 + index / 8 * 18, ICON, ICON, 256, 256);
            }

            int textX = PAD + ICON + 4;
            mc.fontRendererObj.drawStringWithShadow(name(effect), textX, y, 0xFFFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(Potion.getDurationString(effect), textX, y + 10, 0xFFFFFFFF);
        }
    }
}
