package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.TextHudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Tap your sneak key to start sneaking, tap it again to stop. */
public class ToggleSneak extends TextHudModule {
    private final BooleanSetting showText = add(new BooleanSetting("Show text", true));
    private boolean toggled;

    public ToggleSneak() {
        super("Toggle Sneak", "Tap sneak to keep sneaking", Category.PVP, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.START, 2, 14);
        background.set(false);
        brackets.set(true);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.leather_boots);
    }

    @Override
    public void onTick() {
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        while (sneak.isPressed()) {
            if (mc.currentScreen == null) toggled = !toggled;
        }
        if (toggled && sneak.getKeyCode() != 0) KeyBinding.setKeyBindState(sneak.getKeyCode(), true);
    }

    @Override
    public void onDisable() {
        if (toggled && mc.gameSettings.keyBindSneak.getKeyCode() != 0) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
        toggled = false;
    }

    @Override
    protected String getText() {
        return showText.isOn() && toggled ? "Sneaking (Toggled)" : null;
    }

    @Override
    protected String getPreviewText() {
        return "Sneaking (Toggled)";
    }
}
