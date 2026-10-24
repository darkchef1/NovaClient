package com.nova.module;

import com.nova.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for every mod in the client.
 * <p>
 * Override {@link #onTick()}, {@link #onEnable()} or {@link #onDisable()} for simple mods.
 * Modules are also registered on the Forge event bus, so you can add {@code @SubscribeEvent}
 * methods for any Forge event, but those must start with {@code if (!isEnabled()) return;}.
 */
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<>();
    private boolean enabled;
    private int key = Keyboard.KEY_NONE;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    /** Registers a setting so it shows in the menu and gets saved. */
    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    /** Called every client tick while enabled and in a world. */
    public void onTick() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    /** The item shown as this mod's icon in the menu. */
    public ItemStack getIcon() {
        return new ItemStack(Items.paper);
    }

    /** Whether this mod appears in the Active Mods list when enabled. */
    public boolean showInModList() {
        return true;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
