package com.nova.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nova.util.ServerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
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

/** All waypoints, saved to config/novaclient/waypoints.json (shared by every profile). */
public final class WaypointManager {
    private static final Logger LOGGER = LogManager.getLogger("NovaClient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Waypoint> waypoints = new ArrayList<>();
    private static File file;

    private WaypointManager() {
    }

    public static void load(File waypointFile) {
        file = waypointFile;
        waypoints.clear();
        if (!file.exists()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<Waypoint> loaded = GSON.fromJson(reader, new TypeToken<List<Waypoint>>() {}.getType());
            if (loaded != null) waypoints.addAll(loaded);
        } catch (Exception e) {
            LOGGER.error("Could not load " + file, e);
        }
    }

    public static void save() {
        if (file == null) return;
        file.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(waypoints, writer);
        } catch (Exception e) {
            LOGGER.error("Could not save " + file, e);
        }
    }

    /** Waypoints on the server you're on (all dimensions), or every waypoint when you're in the menus. */
    public static List<Waypoint> forCurrentServer() {
        String server = ServerUtil.currentServer();
        if (server.equals("none")) return Collections.unmodifiableList(waypoints);
        List<Waypoint> result = new ArrayList<>();
        for (Waypoint w : waypoints) if (w.server.equals(server)) result.add(w);
        return result;
    }

    /** Waypoints to draw right now: this server and the dimension you're in. */
    public static List<Waypoint> forCurrentWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        List<Waypoint> result = new ArrayList<>();
        if (mc.thePlayer == null) return result;
        for (Waypoint w : forCurrentServer()) {
            if (w.dimension == mc.thePlayer.dimension) result.add(w);
        }
        return result;
    }

    /** Adds a waypoint where you're standing. */
    public static Waypoint addHere() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return null;
        Waypoint w = new Waypoint("Waypoint " + (forCurrentServer().size() + 1),
                MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posY),
                MathHelper.floor_double(mc.thePlayer.posZ), mc.thePlayer.dimension,
                ServerUtil.currentServer(), waypoints.size());
        waypoints.add(w);
        save();
        return w;
    }

    public static void remove(Waypoint w) {
        waypoints.remove(w);
        save();
    }
}
