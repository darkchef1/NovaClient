package com.nova.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nova.ClientSettings;
import com.nova.module.HudModule;
import com.nova.module.Module;
import com.nova.module.ModuleManager;
import com.nova.setting.Setting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/** Saves which mods are on, their keybinds, settings and HUD positions to config/novaclient.json. */
public final class Config {
    private static final Logger LOGGER = LogManager.getLogger("NovaClient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CLIENT_KEY = "_client";

    private final File file;

    public Config(File file) {
        this.file = file;
    }

    /** @return false if there was no config yet (first launch). */
    public boolean load(ModuleManager manager, ClientSettings clientSettings) {
        if (!file.exists()) return false;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            for (Module module : manager.getModules()) {
                loadModule(module, root.get(module.getName()));
            }
            loadModule(clientSettings, root.get(CLIENT_KEY));
            return true;
        } catch (Exception e) {
            LOGGER.error("Could not load " + file, e);
            return false;
        }
    }

    private void loadModule(Module module, JsonElement element) {
        if (element == null || !element.isJsonObject()) return;
        JsonObject data = element.getAsJsonObject();

        if (data.has("enabled")) module.setEnabled(data.get("enabled").getAsBoolean());
        if (data.has("key")) module.setKey(data.get("key").getAsInt());
        if (module instanceof HudModule) {
            HudModule hud = (HudModule) module;
            if (data.has("anchorX")) {
                hud.loadPosition(
                        HudModule.Anchor.valueOf(data.get("anchorX").getAsString()),
                        HudModule.Anchor.valueOf(data.get("anchorY").getAsString()),
                        data.get("offsetX").getAsFloat(),
                        data.get("offsetY").getAsFloat(),
                        data.has("scale") ? data.get("scale").getAsFloat() : 1f);
            } else if (data.has("x") && data.has("y")) {
                // config from version 1.0
                hud.loadPosition(HudModule.Anchor.START, HudModule.Anchor.START,
                        data.get("x").getAsFloat(), data.get("y").getAsFloat(), 1f);
            }
        }
        JsonObject settings = data.has("settings") ? data.getAsJsonObject("settings") : new JsonObject();
        for (Setting setting : module.getSettings()) {
            if (settings.has(setting.getName())) setting.load(settings.get(setting.getName()).getAsString());
        }
    }

    public void save(ModuleManager manager, ClientSettings clientSettings) {
        JsonObject root = new JsonObject();
        for (Module module : manager.getModules()) {
            root.add(module.getName(), saveModule(module));
        }
        root.add(CLIENT_KEY, saveModule(clientSettings));

        file.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            LOGGER.error("Could not save " + file, e);
        }
    }

    private JsonObject saveModule(Module module) {
        JsonObject data = new JsonObject();
        data.addProperty("enabled", module.isEnabled());
        data.addProperty("key", module.getKey());
        if (module instanceof HudModule) {
            HudModule hud = (HudModule) module;
            data.addProperty("anchorX", hud.getAnchorX().name());
            data.addProperty("anchorY", hud.getAnchorY().name());
            data.addProperty("offsetX", hud.getOffsetX());
            data.addProperty("offsetY", hud.getOffsetY());
            data.addProperty("scale", hud.getScale());
        }
        JsonObject settings = new JsonObject();
        for (Setting setting : module.getSettings()) {
            settings.addProperty(setting.getName(), setting.save());
        }
        data.add("settings", settings);
        return data;
    }
}
