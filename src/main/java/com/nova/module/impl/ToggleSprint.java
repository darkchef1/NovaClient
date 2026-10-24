package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Holds the sprint key down for you while enabled. Bind a key to it in its settings to toggle quickly. */
public class ToggleSprint extends TextHudModule {
    private final BooleanSetting showText = add(new BooleanSetting("Show text", true));

    public ToggleSprint() {
        super("Toggle Sprint", "Sprint without holding the sprint key", Category.PVP, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.START, 2, 2);
        // Lunar shows this as plain "[Sprinting (Toggled)]" text with no box
        background.set(false);
        brackets.set(true);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.sugar);
    }

    @Override
    public void onTick() {
        int sprintKey = mc.gameSettings.keyBindSprint.getKeyCode();
        if (sprintKey != 0) KeyBinding.setKeyBindState(sprintKey, true);
    }

    @Override
    public void onDisable() {
        int sprintKey = mc.gameSettings.keyBindSprint.getKeyCode();
        if (sprintKey != 0) KeyBinding.setKeyBindState(sprintKey, false);
    }

    @Override
    protected String getText() {
        if (!showText.isOn()) return null;
        return mc.thePlayer.isSprinting() ? "Sprinting (Toggled)" : "Sprint (Toggled)";
    }

    @Override
    protected String getPreviewText() {
        return "Sprinting (Toggled)";
    }
}
