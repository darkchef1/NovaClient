package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.Render;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * A countdown circle after you throw an ender pearl, like Lunar's on Minemen.
 * Servers only tell Lunar about cooldowns, so this starts when your pearl count drops
 * while holding a pearl, and counts down the cooldown you set (Minemen uses 16 seconds).
 */
public class PearlCooldown extends HudModule {
    private static final int SIZE = 22;
    private static final int PAD = 3;

    private final NumberSetting cooldown = add(new NumberSetting("Cooldown", 16, 1, 30, 0.5, "s"));
    private final BooleanSetting hideWhenReady = add(new BooleanSetting("Hide when ready", true));
    private final BooleanSetting background = add(new BooleanSetting("Background", true));

    private long thrownAt;
    private int lastCount = -1;
    private boolean wasHoldingPearl;

    public PearlCooldown() {
        super("Pearl Cooldown", "Shows a countdown after you throw an ender pearl", Category.PVP, 2, 2);
        setDefaultPosition(Anchor.CENTER, Anchor.END, 0, 62);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.ender_pearl);
    }

    @Override
    public void onTick() {
        int count = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem() == Items.ender_pearl) count += stack.stackSize;
        }
        if (lastCount >= 0 && count < lastCount && wasHoldingPearl && mc.currentScreen == null) {
            thrownAt = System.currentTimeMillis();
        }
        lastCount = count;
        ItemStack held = mc.thePlayer.getHeldItem();
        wasHoldingPearl = held != null && held.getItem() == Items.ender_pearl;
    }

    private float secondsLeft() {
        float left = cooldown.getFloat() - (System.currentTimeMillis() - thrownAt) / 1000f;
        return Math.max(0f, left);
    }

    private String label(float left) {
        return left > 0 ? String.format("%.1fs", left) : "Ready";
    }

    @Override
    public int getWidth() {
        return PAD * 2 + SIZE + 5 + mc.fontRendererObj.getStringWidth("88.8s");
    }

    @Override
    public int getHeight() {
        return PAD * 2 + SIZE;
    }

    @Override
    public void render(boolean preview) {
        float left = preview && secondsLeft() == 0 ? cooldown.getFloat() * 0.6f : secondsLeft();
        if (left == 0 && hideWhenReady.isOn() && !preview) return;
        if (background.isOn()) Gui.drawRect(0, 0, getWidth(), getHeight(), 0x6F000000);

        float cx = PAD + SIZE / 2f;
        float cy = PAD + SIZE / 2f;
        float progress = left / cooldown.getFloat();
        Render.arc(cx, cy, SIZE / 2f, 2.5f, 0, 360, 0x50FFFFFF);
        Render.arc(cx, cy, SIZE / 2f, 2.5f, 0, 360 * progress, left > 0 ? 0xFFFFFFFF : NovaClient.accent());
        Render.item(new ItemStack(Items.ender_pearl), cx - 8, cy - 8, 1f);
        mc.fontRendererObj.drawStringWithShadow(label(left), PAD + SIZE + 5, cy - 4, 0xFFFFFFFF);
    }
}
