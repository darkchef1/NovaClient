package com.nova.setting;

/** A number picked with a slider. */
public class NumberSetting extends Setting {
    private final double min;
    private final double max;
    private final double step;
    private final String suffix;
    private double value;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, defaultValue, min, max, step, "");
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, String suffix) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
        set(defaultValue);
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public void set(double value) {
        value = Math.max(min, Math.min(max, value));
        this.value = Math.round(value / step) * step;
    }

    /** Position of the slider knob, 0 to 1. */
    public double getPercent() {
        return (value - min) / (max - min);
    }

    public void setPercent(double percent) {
        set(min + (max - min) * Math.max(0, Math.min(1, percent)));
    }

    @Override
    public String getDisplayValue() {
        String number = step >= 1 ? Integer.toString(getInt()) : String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
        return number + suffix;
    }

    @Override
    public void click(int button) {
        set(value + (button == 1 ? -step : step));
    }

    @Override
    public String save() {
        return Double.toString(value);
    }

    @Override
    public void load(String value) {
        try {
            set(Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
        }
    }
}
