package com.nova.cosmetic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The cosmetics catalog, what you're wearing, and what other Nova players are wearing.
 * <p>
 * Your own choice is saved in config/novaclient/cosmetics.json. Other players' cosmetics come from
 * cosmetics.json on the Nova GitHub page, checked every few minutes; to give a friend a cape,
 * add their Minecraft name there. Only Nova players can see Nova cosmetics.
 */
public final class Cosmetics {
    private static final Logger LOGGER = LogManager.getLogger("NovaClient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SHARED_URL = "https://raw.githubusercontent.com/darkchef1/NovaClient/main/cosmetics.json";
    private static final long REFRESH_MS = 5 * 60 * 1000;
    public static final String CUSTOM_PREFIX = "custom:";

    private static final List<Cosmetic> BUILT_IN = Arrays.asList(
            new Cosmetic("nova", "Nova", CosmeticSlot.CAPE, false),
            new Cosmetic("galaxy", "Galaxy", CosmeticSlot.CAPE, true),
            new Cosmetic("aurora", "Aurora", CosmeticSlot.CAPE, true),
            new Cosmetic("rainbow", "Rainbow", CosmeticSlot.CAPE, true),
            new Cosmetic("sunset", "Sunset", CosmeticSlot.CAPE, false),
            new Cosmetic("ice", "Frost", CosmeticSlot.CAPE, false),
            new Cosmetic("crimson", "Crimson", CosmeticSlot.CAPE, false),
            new Cosmetic("midnight", "Midnight", CosmeticSlot.CAPE, false),
            new Cosmetic("lava", "Lava", CosmeticSlot.CAPE, true),
            new Cosmetic("flame", "Flame", CosmeticSlot.CAPE, true),
            new Cosmetic("matrix", "Matrix", CosmeticSlot.CAPE, true),
            new Cosmetic("neon", "Neon", CosmeticSlot.CAPE, true),
            new Cosmetic("storm", "Storm", CosmeticSlot.CAPE, true),
            new Cosmetic("void", "Void", CosmeticSlot.CAPE, true),
            new Cosmetic("ocean", "Ocean", CosmeticSlot.CAPE, false),
            new Cosmetic("forest", "Forest", CosmeticSlot.CAPE, false),
            new Cosmetic("sakura", "Sakura", CosmeticSlot.CAPE, false),
            new Cosmetic("gold", "Gold", CosmeticSlot.CAPE, false),
            new Cosmetic("checker", "Checker", CosmeticSlot.CAPE, false),
            new Cosmetic("candy", "Candy", CosmeticSlot.CAPE, false),
            new Cosmetic("angel", "Angel", CosmeticSlot.WINGS, true),
            new Cosmetic("dragon", "Dragon", CosmeticSlot.WINGS, true),
            new Cosmetic("crystal", "Nova Crystal", CosmeticSlot.WINGS, true),
            new Cosmetic("phoenix", "Phoenix", CosmeticSlot.WINGS, true),
            new Cosmetic("demon", "Demon", CosmeticSlot.WINGS, true),
            new Cosmetic("butterfly", "Butterfly", CosmeticSlot.WINGS, true),
            new Cosmetic("fairy", "Fairy", CosmeticSlot.WINGS, true),
            new Cosmetic("bat", "Bat", CosmeticSlot.WINGS, true),
            new Cosmetic("halo", "Halo", CosmeticSlot.HEAD, true),
            new Cosmetic("rainbow_halo", "Rainbow Halo", CosmeticSlot.HEAD, true),
            new Cosmetic("crown", "Crown", CosmeticSlot.HEAD, false),
            new Cosmetic("horns", "Devil Horns", CosmeticSlot.HEAD, false),
            new Cosmetic("cat_ears", "Cat Ears", CosmeticSlot.HEAD, false),
            new Cosmetic("bunny_ears", "Bunny Ears", CosmeticSlot.HEAD, false),
            new Cosmetic("top_hat", "Top Hat", CosmeticSlot.HEAD, false),
            new Cosmetic("flower_crown", "Flower Crown", CosmeticSlot.HEAD, false),
            new Cosmetic("stars", "Star Aura", CosmeticSlot.AURA, true),
            new Cosmetic("rainbow_stars", "Rainbow Aura", CosmeticSlot.AURA, true),
            new Cosmetic("hearts", "Love", CosmeticSlot.AURA, true),
            new Cosmetic("embers", "Embers", CosmeticSlot.AURA, true),
            new Cosmetic("snow", "Snowfall", CosmeticSlot.AURA, true),
            new Cosmetic("atom", "Atom", CosmeticSlot.AURA, true));

    private static final EnumMap<CosmeticSlot, String> equipped = new EnumMap<>(CosmeticSlot.class);
    private static final List<Cosmetic> customCapes = new ArrayList<>();
    private static volatile Map<String, EnumMap<CosmeticSlot, String>> shared = Collections.emptyMap();
    private static File file;
    private static File capesFolder;

    private Cosmetics() {
    }

    public static void init(File configDir) {
        file = new File(configDir, "cosmetics.json");
        capesFolder = new File(configDir, "capes");
        capesFolder.mkdirs();
        load();
        scanCustomCapes();

        Thread fetcher = new Thread(() -> {
            while (true) {
                fetchShared();
                try {
                    Thread.sleep(REFRESH_MS);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "Nova cosmetics");
        fetcher.setDaemon(true);
        fetcher.start();
    }

    // ------------------------------------------------------------------ catalog

    /** Everything for a slot, with your own capes (from the capes folder) at the end. */
    public static List<Cosmetic> catalog(CosmeticSlot slot) {
        List<Cosmetic> list = new ArrayList<>();
        for (Cosmetic c : BUILT_IN) if (c.slot == slot) list.add(c);
        if (slot == CosmeticSlot.CAPE) list.addAll(customCapes);
        return list;
    }

    /** Finds PNG images in config/novaclient/capes and adds them as capes only you see. */
    public static void scanCustomCapes() {
        customCapes.clear();
        File[] files = capesFolder == null ? null : capesFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".png"));
        if (files == null) return;
        Arrays.sort(files);
        for (File f : files) {
            String name = f.getName().substring(0, f.getName().length() - 4);
            customCapes.add(new Cosmetic(CUSTOM_PREFIX + f.getName(), name, CosmeticSlot.CAPE, false));
        }
    }

    public static File getCapesFolder() {
        return capesFolder;
    }

    // ------------------------------------------------------------------ what you're wearing

    public static String getEquipped(CosmeticSlot slot) {
        return equipped.get(slot);
    }

    public static Map<CosmeticSlot, String> getEquipped() {
        return Collections.unmodifiableMap(equipped);
    }

    /** Wears a cosmetic, or takes off whatever is in that slot when id is null. */
    public static void equip(CosmeticSlot slot, String id) {
        if (id == null) equipped.remove(slot);
        else equipped.put(slot, id);
        save();
    }

    // ------------------------------------------------------------------ players

    private static boolean isYou(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        return player == mc.thePlayer || player.getGameProfile().getName().equalsIgnoreCase(mc.getSession().getUsername());
    }

    /** What a player is wearing, or null if they have no Nova cosmetics. */
    public static Map<CosmeticSlot, String> forPlayer(EntityPlayer player) {
        if (isYou(player)) return equipped;
        return shared.get(player.getGameProfile().getName().toLowerCase(Locale.ROOT));
    }

    /** Players on the shared list get the Nova star next to their name. */
    public static boolean isNovaPlayer(EntityPlayer player) {
        return shared.containsKey(player.getGameProfile().getName().toLowerCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------ saving and the shared list

    private static void load() {
        if (file == null || !file.exists()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            equipped.putAll(parseSlots(new JsonParser().parse(reader).getAsJsonObject()));
        } catch (Exception e) {
            LOGGER.error("Could not load " + file, e);
        }
    }

    private static void save() {
        if (file == null) return;
        JsonObject root = new JsonObject();
        for (Map.Entry<CosmeticSlot, String> e : equipped.entrySet()) root.addProperty(e.getKey().key, e.getValue());
        file.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            LOGGER.error("Could not save " + file, e);
        }
    }

    private static EnumMap<CosmeticSlot, String> parseSlots(JsonObject object) {
        EnumMap<CosmeticSlot, String> slots = new EnumMap<>(CosmeticSlot.class);
        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
            CosmeticSlot slot = CosmeticSlot.byKey(e.getKey());
            // other players can't use custom capes: those image files only exist on their own PC
            if (slot != null && e.getValue().isJsonPrimitive() && !e.getValue().getAsString().startsWith(CUSTOM_PREFIX)) {
                slots.put(slot, e.getValue().getAsString());
            }
        }
        return slots;
    }

    /** Reads {"players": {"name": {"cape": "galaxy", ...}}} from the Nova GitHub page. */
    private static void fetchShared() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(SHARED_URL).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "NovaClient");
            if (connection.getResponseCode() != 200) return;
            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject players = new JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("players");
                Map<String, EnumMap<CosmeticSlot, String>> next = new HashMap<>();
                if (players != null) {
                    for (Map.Entry<String, JsonElement> e : players.entrySet()) {
                        if (e.getValue().isJsonObject()) next.put(e.getKey().toLowerCase(Locale.ROOT), parseSlots(e.getValue().getAsJsonObject()));
                    }
                }
                shared = next;
            }
        } catch (Exception e) {
            // offline or GitHub unreachable: keep the last list
        }
    }
}
