package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.settings.KeyBinding;

/** Holds the sprint key down for you while enabled. Bind a key to it in its settings to toggle quickly. */
public class ToggleSprint extends TextHudModule {
    private final BooleanSetting showText = add(new BooleanSetting("Show text", true));

    public ToggleSprint() {
        super("Toggle Sprint", "Sprint without holding the sprint key", Category.PVP, 2, 74);
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

    @Override
    public net.minecraft.item.ItemStack getIcon() {
        return new net.minecraft.item.ItemStack(net.minecraft.init.Items.sugar);
    }
}
