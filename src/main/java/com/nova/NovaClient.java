package com.nova;

import com.nova.config.Config;
import com.nova.event.ClientEvents;
import com.nova.module.ModuleManager;
import com.nova.util.CpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;

@Mod(modid = NovaClient.MODID, useMetadata = true)
public class NovaClient {
    public static final String MODID = "novaclient";
    public static final String NAME = "Nova Client";

    /** Opens the mod menu. Rebindable in Options > Controls. */
    public static final KeyBinding MENU_KEY = new KeyBinding("Open mod menu", Keyboard.KEY_RSHIFT, NAME);

    private static ModuleManager moduleManager;
    private static ClientSettings settings;
    private static Config config;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManager = new ModuleManager();
        settings = new ClientSettings();
        config = new Config(new File(Minecraft.getMinecraft().mcDataDir, "config/novaclient.json"));
        moduleManager.enableDefaults(config.load(moduleManager, settings));

        ClientRegistry.registerKeyBinding(MENU_KEY);
        MinecraftForge.EVENT_BUS.register(new CpsTracker());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static ClientSettings getSettings() {
        return settings;
    }

    /** The accent color picked in the SETTINGS tab. */
    public static int accent() {
        return settings.getAccent();
    }

    public static void saveConfig() {
        config.save(moduleManager, settings);
    }
}
