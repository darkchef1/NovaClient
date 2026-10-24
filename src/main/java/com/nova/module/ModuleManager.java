package com.nova.module;

import com.nova.module.impl.ActiveMods;
import com.nova.module.impl.ArmorStatus;
import com.nova.module.impl.Coordinates;
import com.nova.module.impl.CpsDisplay;
import com.nova.module.impl.FpsDisplay;
import com.nova.module.impl.Keystrokes;
import com.nova.module.impl.PingDisplay;
import com.nova.module.impl.ToggleSprint;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // ===== Add new mods here: one line each =====
        register(new FpsDisplay());
        register(new CpsDisplay());
        register(new PingDisplay());
        register(new Coordinates());
        register(new ToggleSprint());
        register(new Keystrokes());
        register(new ArmorStatus());
        register(new ActiveMods());
    }

    private void register(Module module) {
        modules.add(module);
        MinecraftForge.EVENT_BUS.register(module);
    }

    /** Mods that are turned on the very first time the client starts. */
    public void enableDefaults() {
        get(FpsDisplay.class).setEnabled(true);
        get(CpsDisplay.class).setEnabled(true);
        get(Keystrokes.class).setEnabled(true);
        get(ArmorStatus.class).setEnabled(true);
        get(ActiveMods.class).setEnabled(true);
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
