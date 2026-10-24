package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.Anim;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * Hold the zoom key (C by default, change it in Options > Controls) to zoom in smoothly.
 * Scroll while zoomed to zoom further. Mouse sensitivity drops while zoomed so aiming stays steady.
 */
public class Zoom extends Module {
    private static final float MIN_ZOOM = 1.5f;
    private static final float MAX_ZOOM = 30f;

    private final KeyBinding zoomKey = new KeyBinding("Zoom", Keyboard.KEY_C, NovaClient.NAME);

    private final ModeSetting mode = add(new ModeSetting("Key mode", "Hold", "Hold", "Toggle"));
    private final NumberSetting amount = add(new NumberSetting("Zoom amount", 4, MIN_ZOOM, 15, 0.5, "x"));
    private final BooleanSetting smooth = add(new BooleanSetting("Smooth zoom", true));
    private final BooleanSetting scrolling = add(new BooleanSetting("Scroll to adjust", true));
    private final BooleanSetting lowerSensitivity = add(new BooleanSetting("Lower sensitivity", true));
    private final BooleanSetting cinematic = add(new BooleanSetting("Cinematic camera", false));
    private final BooleanSetting hideHand = add(new BooleanSetting("Hide hand", true));

    private final Anim zoom = new Anim(14f, 1f);
    private boolean zooming;
    private boolean toggled;
    private float target;
    private float savedSensitivity;
    private boolean savedSmoothCamera;

    public Zoom() {
        super("Zoom", "Hold C to zoom in. Scroll to zoom more", Category.MECHANIC);
        ClientRegistry.registerKeyBinding(zoomKey);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.glass);
    }

    @Override
    public void onTick() {
        boolean pressed = false;
        while (zoomKey.isPressed()) pressed = true;
        if (mode.is("Toggle")) {
            if (pressed) toggled = !toggled;
        } else {
            toggled = zoomKey.isKeyDown();
        }
        boolean want = toggled && mc.currentScreen == null;

        if (want && !zooming) start();
        else if (!want && zooming) stop();
        if (zooming && lowerSensitivity.isOn()) applySensitivity();
    }

    @Override
    public void onDisable() {
        toggled = false;
        if (zooming) stop();
        zoom.snap(1f);
    }

    private void start() {
        zooming = true;
        target = amount.getFloat();
        savedSensitivity = mc.gameSettings.mouseSensitivity;
        savedSmoothCamera = mc.gameSettings.smoothCamera;
        if (cinematic.isOn()) mc.gameSettings.smoothCamera = true;
    }

    private void stop() {
        zooming = false;
        if (lowerSensitivity.isOn()) mc.gameSettings.mouseSensitivity = savedSensitivity;
        mc.gameSettings.smoothCamera = savedSmoothCamera;
    }

    /**
     * Minecraft turns the sensitivity slider s into a speed of (0.6s + 0.2)^3.
     * Dividing that speed by the zoom keeps aiming feeling the same when zoomed.
     */
    private void applySensitivity() {
        float speed = savedSensitivity * 0.6f + 0.2f;
        float zoomedSpeed = speed / (float) Math.cbrt(target);
        mc.gameSettings.mouseSensitivity = (zoomedSpeed - 0.2f) / 0.6f;
    }

    /** How zoomed in we are right now: 1 = normal, 4 = four times closer. */
    private float currentZoom() {
        zoom.setSpeed(smooth.isOn() ? 14f : 1000f);
        zoom.setTarget(zooming ? target : 1f);
        return zoom.get();
    }

    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        if (!isEnabled()) return;
        float factor = currentZoom();
        if (factor > 1.001f) event.setFOV(event.getFOV() / factor);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (!isEnabled() || !hideHand.isOn()) return;
        if (currentZoom() > 1.05f) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onScroll(MouseEvent event) {
        if (!isEnabled() || !zooming || !scrolling.isOn() || event.dwheel == 0) return;
        target = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, target * (event.dwheel > 0 ? 1.25f : 0.8f)));
        event.setCanceled(true);
    }
}
