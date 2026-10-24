package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.ModeSetting;
import com.nova.util.ServerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * Hold Left Alt to look around without turning your character.
 * Hypixel asks clients not to allow this, so it does nothing on Hypixel.
 * Applied by MixinEntity (mouse moves the camera instead of the player)
 * and MixinEntityRenderer (the camera uses the freelook angles).
 */
public class Freelook extends Module {
    private static boolean active;
    private static float cameraYaw;
    private static float cameraPitch;

    private final KeyBinding key = new KeyBinding("Freelook", Keyboard.KEY_LMENU, NovaClient.NAME);
    private final ModeSetting mode = add(new ModeSetting("Key mode", "Hold", "Hold", "Toggle"));
    private final ModeSetting view = add(new ModeSetting("View", "Third person", "Third person", "Keep current"));

    private boolean toggled;
    private int savedView;

    public Freelook() {
        super("Freelook", "Hold Left Alt to look around (off on Hypixel)", Category.MECHANIC);
        ClientRegistry.registerKeyBinding(key);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.ender_eye);
    }

    @Override
    public void onTick() {
        boolean pressed = false;
        while (key.isPressed()) pressed = true;
        if (mode.is("Toggle")) {
            if (pressed) toggled = !toggled;
        } else {
            toggled = key.isKeyDown();
        }
        boolean want = toggled && mc.currentScreen == null && !ServerUtil.isHypixel();
        if (want && !active) start();
        else if (!want && active) stop();
    }

    @Override
    public void onDisable() {
        toggled = false;
        if (active) stop();
    }

    private void start() {
        cameraYaw = mc.thePlayer.rotationYaw;
        cameraPitch = mc.thePlayer.rotationPitch;
        savedView = mc.gameSettings.thirdPersonView;
        if (view.is("Third person")) mc.gameSettings.thirdPersonView = 1;
        active = true;
    }

    private void stop() {
        active = false;
        mc.gameSettings.thirdPersonView = savedView;
    }

    public static boolean isActive() {
        return active;
    }

    /** Mouse movement while freelooking turns the camera, the same way it would turn the player. */
    public static void turn(float yaw, float pitch) {
        cameraYaw += yaw * 0.15f;
        cameraPitch = Math.max(-90f, Math.min(90f, cameraPitch - pitch * 0.15f));
    }

    public static float yaw(Entity entity, float vanilla) {
        return active && entity == Minecraft.getMinecraft().thePlayer ? cameraYaw : vanilla;
    }

    public static float pitch(Entity entity, float vanilla) {
        return active && entity == Minecraft.getMinecraft().thePlayer ? cameraPitch : vanilla;
    }
}
