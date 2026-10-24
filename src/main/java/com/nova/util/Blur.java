package com.nova.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Method;

/** Blurs the game behind menus using Minecraft's built-in blur shader. */
public final class Blur {
    private static final ResourceLocation BLUR = new ResourceLocation("shaders/post/blur.json");
    private static Method loadShader;
    private static boolean active;

    private Blur() {
    }

    public static void start() {
        Minecraft mc = Minecraft.getMinecraft();
        if (active || mc.theWorld == null || !OpenGlHelper.shadersSupported || mc.entityRenderer.isShaderActive()) return;
        try {
            if (loadShader == null) {
                for (String name : new String[]{"loadShader", "func_175069_a"}) {
                    try {
                        loadShader = EntityRenderer.class.getDeclaredMethod(name, ResourceLocation.class);
                        break;
                    } catch (NoSuchMethodException ignored) {
                    }
                }
                if (loadShader == null) return;
                loadShader.setAccessible(true);
            }
            loadShader.invoke(mc.entityRenderer, BLUR);
            active = true;
        } catch (Exception ignored) {
        }
    }

    public static void stop() {
        if (!active) return;
        active = false;
        Minecraft.getMinecraft().entityRenderer.stopUseShader();
    }
}
