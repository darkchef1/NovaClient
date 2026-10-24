package com.nova.util;

import net.minecraft.client.Minecraft;

public final class ServerUtil {
    private ServerUtil() {
    }

    /** The server's address in lower case, "singleplayer:<world>", or "none" in the menus. */
    public static String currentServer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getCurrentServerData() != null) return mc.getCurrentServerData().serverIP.toLowerCase();
        if (mc.isSingleplayer() && mc.getIntegratedServer() != null) {
            return "singleplayer:" + mc.getIntegratedServer().getWorldName();
        }
        return "none";
    }

    /** Hypixel asks clients to turn off some features (like freelook) on its servers. */
    public static boolean isHypixel() {
        String server = currentServer();
        return server.endsWith("hypixel.net") || server.contains("hypixel.net:");
    }
}
