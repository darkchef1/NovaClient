package com.nova.setting;

/** A setting that cycles through a fixed list of choices. */
public class ModeSetting extends Setting {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = modes;
        load(defaultMode);
    }

    public String getMode() {
        return modes[index];
    }

    public boolean is(String mode) {
        return getMode().equals(mode);
    }

    @Override
    public String getDisplayValue() {
        return getMode();
    }

    @Override
    public void click(int button) {
        int step = button == 1 ? modes.length - 1 : 1;
        index = (index + step) % modes.length;
    }

    @Override
    public String save() {
        return getMode();
    }

    @Override
    public void load(String value) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) {
                index = i;
                return;
            }
        }
    }
}
