package com.nova.module;

import com.nova.module.impl.ActiveMods;
import com.nova.module.impl.ArmorStatus;
import com.nova.module.impl.AutoGG;
import com.nova.module.impl.BlockOutline;
import com.nova.module.impl.ChatTimestamps;
import com.nova.module.impl.Clock;
import com.nova.module.impl.ComboCounter;
import com.nova.module.impl.CompactChat;
import com.nova.module.impl.Coordinates;
import com.nova.module.impl.CpsDisplay;
import com.nova.module.impl.CustomCrosshair;
import com.nova.module.impl.DayCounter;
import com.nova.module.impl.FpsDisplay;
import com.nova.module.impl.Freelook;
import com.nova.module.impl.Fullbright;
import com.nova.module.impl.HitColor;
import com.nova.module.impl.ItemCounter;
import com.nova.module.impl.ItemPhysics;
import com.nova.module.impl.Keystrokes;
import com.nova.module.impl.MotionBlur;
import com.nova.module.impl.NoHurtShake;
import com.nova.module.impl.OldAnimations;
import com.nova.module.impl.PearlCooldown;
import com.nova.module.impl.PingDisplay;
import com.nova.module.impl.PotionEffects;
import com.nova.module.impl.ReachDisplay;
import com.nova.module.impl.TimeChanger;
import com.nova.module.impl.ToggleSprint;
import com.nova.module.impl.Waypoints;
import com.nova.module.impl.Zoom;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    /** Mods that start turned on (the first time they're seen). */
    private static final List<Class<? extends Module>> ON_BY_DEFAULT = Arrays.asList(
            PingDisplay.class, CpsDisplay.class, FpsDisplay.class, Clock.class,
            Keystrokes.class, PotionEffects.class, ArmorStatus.class, Zoom.class,
            ItemCounter.class, PearlCooldown.class, OldAnimations.class, Waypoints.class);

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // ===== Add new mods here: one line each. The menu shows them in this order. =====
        // HUD
        register(new PingDisplay());
        register(new CpsDisplay());
        register(new FpsDisplay());
        register(new Clock());
        register(new DayCounter());
        register(new Coordinates());
        register(new Keystrokes());
        register(new PotionEffects());
        register(new ArmorStatus());
        register(new ActiveMods());
        // PvP
        register(new ItemCounter());
        register(new PearlCooldown());
        register(new ComboCounter());
        register(new ReachDisplay());
        register(new HitColor());
        register(new CustomCrosshair());
        register(new NoHurtShake());
        register(new ToggleSprint());
        // Mechanic / visual
        register(new Zoom());
        register(new OldAnimations());
        register(new ItemPhysics());
        register(new BlockOutline());
        register(new Fullbright());
        register(new TimeChanger());
        register(new MotionBlur());
        register(new Freelook());
        register(new Waypoints());
        // Chat
        register(new AutoGG());
        register(new CompactChat());
        register(new ChatTimestamps());
    }

    private void register(Module module) {
        modules.add(module);
        MinecraftForge.EVENT_BUS.register(module);
    }

    /** Turns on the default mods among these (new mods, or all of them on first launch). */
    public void enableDefaults(Collection<Module> newModules) {
        for (Module module : newModules) {
            if (ON_BY_DEFAULT.contains(module.getClass())) module.setEnabled(true);
        }
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<HudModule> getHudModules() {
        List<HudModule> hud = new ArrayList<>();
        for (Module module : modules) {
            if (module instanceof HudModule) hud.add((HudModule) module);
        }
        return hud;
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) return type.cast(module);
        }
        throw new IllegalArgumentException("Module not registered: " + type.getSimpleName());
    }
}
