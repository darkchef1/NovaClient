package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.emote.EmotePlayer;
import com.nova.gui.EmoteWheel;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * Hold B for the emote wheel (change the key in Options > Controls).
 * Emotes are only on your screen: other players don't see them.
 */
public class Emotes extends Module {
    private final KeyBinding key = new KeyBinding("Emote wheel", Keyboard.KEY_B, NovaClient.NAME);
    private final BooleanSetting frontCamera = add(new BooleanSetting("Turn camera to the front", true));

    public Emotes() {
        super("Emotes", "Hold B to wave, dab, floss and more", Category.MISC);
        ClientRegistry.registerKeyBinding(key);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.slime_ball);
    }

    @Override
    public void onTick() {
        while (key.isPressed()) {
            if (mc.currentScreen == null) mc.displayGuiScreen(new EmoteWheel(key, frontCamera.isOn()));
        }
        EmotePlayer.tick();
    }

    @Override
    public void onDisable() {
        EmotePlayer.stop();
    }
}
