package com.nova.setting;

import com.nova.module.Module;
import org.lwjgl.input.Keyboard;

/** Shows and changes a mod's toggle key in its settings page. The key itself is saved with the mod. */
public class KeybindSetting extends Setting {
    private final Module module;

    public KeybindSetting(Module module) {
        super("Toggle key");
        this.module = module;
    }

    public void setKey(int key) {
        module.setKey(key);
    }

    @Override
    public String getDisplayValue() {
        return module.getKey() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(module.getKey());
    }

    @Override
    public void click(int button) {
    }

    @Override
    public String save() {
        return Integer.toString(module.getKey());
    }

    @Override
    public void load(String value) {
    }
}
