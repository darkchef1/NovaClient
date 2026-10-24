package com.nova.setting;

public class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public boolean isOn() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public String getDisplayValue() {
        return value ? "ON" : "OFF";
    }

    @Override
    public void click(int button) {
        value = !value;
    }

    @Override
    public String save() {
        return Boolean.toString(value);
    }

    @Override
    public void load(String value) {
        this.value = Boolean.parseBoolean(value);
    }
}
