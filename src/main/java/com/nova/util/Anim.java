package com.nova.util;

/**
 * A value that glides smoothly toward a target, independent of FPS.
 * Call {@link #get()} every frame; set where it should go with {@link #setTarget(float)}.
 */
public final class Anim {
    private float speed;
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

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTarget() {
        return target;
    }

    /** Fast at first, gently slowing down at the end. t goes from 0 to 1. */
    public static float easeOut(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    /**
     * Progress (0 to 1, eased) of an item in a cascade: each item starts a little after the previous one.
     * Used to make cards and rows slide in one after another.
     */
    public static float cascade(long startMillis, int index) {
        long elapsed = System.currentTimeMillis() - startMillis - index * 25L;
        return easeOut(elapsed / 240f);
    }

    /** Jump straight to a value with no animation. */
    public void snap(float value) {
        this.value = value;
        this.target = value;
        this.lastUpdate = System.nanoTime();
    }
}
