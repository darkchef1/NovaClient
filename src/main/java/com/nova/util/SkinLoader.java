package com.nova.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

/**
 * Your own skin, for showing your player outside a world (on the main menu).
 * Minecraft downloads it in the background; until then (or offline) the default Steve/Alex skin is used.
 */
public final class SkinLoader {
    private static ResourceLocation skin;
    private static Boolean slim;
    private static boolean requested;

    private SkinLoader() {
    }

    private static void request() {
        if (requested) return;
        requested = true;
        Minecraft mc = Minecraft.getMinecraft();
        GameProfile profile = mc.getSession().getProfile();
        mc.getSkinManager().loadProfileTextures(profile, (type, location, texture) -> {
            if (type != MinecraftProfileTexture.Type.SKIN) return;
            skin = location;
            slim = "slim".equals(texture.getMetadata("model"));
        }, false);
    }

    public static ResourceLocation getSkin() {
        request();
        return skin != null ? skin : DefaultPlayerSkin.getDefaultSkin(Minecraft.getMinecraft().getSession().getProfile().getId());
    }

    /** True for skins with thin (3 pixel) arms, like Alex. */
    public static boolean isSlim() {
        request();
        if (slim != null) return slim;
        return "slim".equals(DefaultPlayerSkin.getSkinType(Minecraft.getMinecraft().getSession().getProfile().getId()));
    }
}
