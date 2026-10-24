package com.nova.util;

/**
 * A value that glides smoothly toward a target, independent of FPS.
 * Call {@link #get()} every frame; set where it should go with {@link #setTarget(float)}.
 */
public final class Anim {
    private final float speed;
    private float value;
    private float target;
    private long lastUpdate = System.nanoTime();

    /** @param speed how fast it moves; around 10-20 feels snappy. */
    public Anim(float speed, float start) {
        this.speed = speed;
        this.value = start;
        this.target = start;
    }

    public float get() {
        long now = System.nanoTime();
        float delta = Math.min(0.1f, (now - lastUpdate) / 1_000_000_000f);
        lastUpdate = now;
        value += (target - value) * Math.min(1f, delta * speed);
        if (Math.abs(target - value) < 0.001f) value = target;
        return value;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getTarget() {
        return target;
    }

    /** Jump straight to a value with no animation. */
    public void snap(float value) {
        this.value = value;
        this.target = value;
        this.lastUpdate = System.nanoTime();
    }
}
