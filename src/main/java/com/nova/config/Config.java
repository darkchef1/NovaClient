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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Saves which mods are on, their keybinds, settings and HUD positions.
 * Each profile is its own file in config/novaclient/profiles/, and client.json remembers the active one.
 */
public final class Config {
    public static final String DEFAULT_PROFILE = "Default";
    private static final Logger LOGGER = LogManager.getLogger("NovaClient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9 _\\-]{1,16}");
    private static final String CLIENT_KEY = "_client";
    private static final String LAYOUT_KEY = "_layout";
    /** Bump this when the default HUD look changes, to give existing setups the new look once. */
    private static final int LAYOUT_VERSION = 2;

    private final File directory;
    private final File profiles;
    private final File clientFile;
    private String active = DEFAULT_PROFILE;

    public Config(File minecraftDir) {
        directory = new File(minecraftDir, "config/novaclient");
        profiles = new File(directory, "profiles");
        clientFile = new File(directory, "client.json");
        profiles.mkdirs();

        // version 1.0 kept everything in config/novaclient.json: that becomes the Default profile
        File legacy = new File(minecraftDir, "config/novaclient.json");
        if (legacy.exists() && !profileFile(DEFAULT_PROFILE).exists() && !legacy.renameTo(profileFile(DEFAULT_PROFILE))) {
            LOGGER.warn("Could not move " + legacy + " into profiles");
        }
        readActiveProfile();
    }

    public File getDirectory() {
        return directory;
    }

    // ------------------------------------------------------------------ profiles

    private File profileFile(String name) {
        return new File(profiles, name + ".json");
    }

    public String getActiveProfile() {
        return active;
    }

    /** Profile names, Default first. */
    public List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        File[] files = profiles.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) names.add(f.getName().substring(0, f.getName().length() - 5));
        }
        names.remove(DEFAULT_PROFILE);
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        names.add(0, DEFAULT_PROFILE);
        if (!names.contains(active)) names.add(active);
        return names;
    }

    /** Saves the current profile, then loads another one. */
    public void switchTo(String name, ModuleManager manager, ClientSettings settings) {
        if (name.equals(active)) return;
        save(manager, settings);
        active = name;
        load(manager, settings);
        writeActiveProfile();
    }

    /** Makes a new profile that starts as a copy of the current one, and switches to it. */
    public String createProfile(ModuleManager manager, ClientSettings settings) {
        save(manager, settings);
        int n = 2;
        while (profileFile("Profile " + n).exists()) n++;
        active = "Profile " + n;
        save(manager, settings);
        return active;
    }

    /** @return false if the new name is invalid or already used. */
    public boolean renameProfile(String from, String to) {
        to = to.trim();
        if (from.equals(DEFAULT_PROFILE) || !VALID_NAME.matcher(to).matches() || profileFile(to).exists()) return false;
        if (!profileFile(from).renameTo(profileFile(to))) return false;
        if (active.equals(from)) {
            active = to;
            writeActiveProfile();
        }
        return true;
    }

    /** Deletes a profile other than Default and the one in use. */
    public boolean deleteProfile(String name) {
        if (name.equals(DEFAULT_PROFILE) || name.equals(active)) return false;
        return profileFile(name).delete();
    }

    private void readActiveProfile() {
        if (!clientFile.exists()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(clientFile), StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (root.has("profile") && profileFile(root.get("profile").getAsString()).exists()) {
                active = root.get("profile").getAsString();
            }
        } catch (Exception e) {
            LOGGER.error("Could not read " + clientFile, e);
        }
    }

    private void writeActiveProfile() {
        JsonObject root = new JsonObject();
        root.addProperty("profile", active);
        write(clientFile, root);
    }

    // ------------------------------------------------------------------ loading and saving

    /**
     * Loads the active profile.
     * @return the mods that weren't in it yet (all of them on first launch),
     * so they can start in their default on/off state.
     */
    public List<Module> load(ModuleManager manager, ClientSettings clientSettings) {
        List<Module> unseen = new ArrayList<>(manager.getModules());
        File file = profileFile(active);
        if (!file.exists()) return unseen;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            // Configs from before the current HUD layout keep on/off and keybinds, but get the new HUD look.
            boolean currentLayout = root.has(LAYOUT_KEY) && root.get(LAYOUT_KEY).getAsInt() >= LAYOUT_VERSION;
            for (Module module : manager.getModules()) {
                JsonElement element = root.get(module.getName());
                if (element != null) unseen.remove(module);
                loadModule(module, element, currentLayout || !(module instanceof HudModule));
            }
            loadModule(clientSettings, root.get(CLIENT_KEY), true);
        } catch (Exception e) {
            LOGGER.error("Could not load " + file, e);
        }
        return unseen;
    }

    private void loadModule(Module module, JsonElement element, boolean loadLook) {
        if (element == null || !element.isJsonObject()) return;
        JsonObject data = element.getAsJsonObject();

        if (data.has("enabled")) module.setEnabled(data.get("enabled").getAsBoolean());
        if (data.has("key")) module.setKey(data.get("key").getAsInt());
        if (!loadLook) return;
        if (module instanceof HudModule) {
            HudModule hud = (HudModule) module;
            if (data.has("anchorX")) {
                hud.loadPosition(
                        HudModule.Anchor.valueOf(data.get("anchorX").getAsString()),
                        HudModule.Anchor.valueOf(data.get("anchorY").getAsString()),
                        data.get("offsetX").getAsFloat(),
                        data.get("offsetY").getAsFloat(),
                        data.has("scale") ? data.get("scale").getAsFloat() : 1f);
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
        root.addProperty(LAYOUT_KEY, LAYOUT_VERSION);
        write(profileFile(active), root);
        writeActiveProfile();
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

    private static void write(File file, JsonObject root) {
        file.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            LOGGER.error("Could not save " + file, e);
        }
    }
}
