package com.nova.setting;

/** An option shown in a mod's settings page. Clicking it changes its value. */
public abstract class Setting {
    private final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getDisplayValue();

    /** @param button 0 = left click (next value), 1 = right click (previous value) */
    public abstract void click(int button);

    public abstract String save();

    public abstract void load(String value);
}
