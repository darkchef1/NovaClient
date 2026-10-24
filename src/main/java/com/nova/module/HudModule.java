package com.nova.module;

import net.minecraft.client.gui.ScaledResolution;

/**
 * A mod that draws something on screen and can be dragged and resized in the HUD editor.
 * {@link #render(boolean)} draws at (0, 0) at scale 1; the client moves and scales it.
 * <p>
 * Positions are anchored to the nearest side of the screen, so an element placed in the
 * bottom-right corner stays there when the window size or GUI scale changes.
 */
public abstract class HudModule extends Module {
    public enum Anchor {START, CENTER, END}

    private Anchor anchorX = Anchor.START;
    private Anchor anchorY = Anchor.START;
    private float offsetX;
    private float offsetY;
    private float scale = 1f;

    private Anchor defaultAnchorX = Anchor.START;
    private Anchor defaultAnchorY = Anchor.START;
    private float defaultOffsetX;
    private float defaultOffsetY;

    protected HudModule(String name, String description, Category category, int defaultX, int defaultY) {
        super(name, description, category);
        setDefaultPosition(Anchor.START, Anchor.START, defaultX, defaultY);
    }

    /** Sets where this element starts, e.g. (END, START, 2, 2) = top-right corner. */
    protected void setDefaultPosition(Anchor anchorX, Anchor anchorY, float offsetX, float offsetY) {
        defaultAnchorX = anchorX;
        defaultAnchorY = anchorY;
        defaultOffsetX = offsetX;
        defaultOffsetY = offsetY;
        resetPosition();
    }

    public void resetPosition() {
        anchorX = defaultAnchorX;
        anchorY = defaultAnchorY;
        offsetX = defaultOffsetX;
        offsetY = defaultOffsetY;
        scale = 1f;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    /**
     * @param preview true inside the HUD editor: draw placeholder content even when
     *                there is nothing to show, so the element can still be dragged.
     */
    public abstract void render(boolean preview);

    public float getScaledWidth() {
        return getWidth() * scale;
    }

    public float getScaledHeight() {
        return getHeight() * scale;
    }

    /** Left edge on screen, in GUI pixels. */
    public float getX() {
        float screen = screenWidth();
        float w = getScaledWidth();
        float x;
        switch (anchorX) {
            case END: x = screen - offsetX - w; break;
            case CENTER: x = screen / 2f + offsetX - w / 2f; break;
            default: x = offsetX;
        }
        return Math.max(0, Math.min(x, screen - w));
    }

    /** Top edge on screen, in GUI pixels. */
    public float getY() {
        float screen = screenHeight();
        float h = getScaledHeight();
        float y;
        switch (anchorY) {
            case END: y = screen - offsetY - h; break;
            case CENTER: y = screen / 2f + offsetY - h / 2f; break;
            default: y = offsetY;
        }
        return Math.max(0, Math.min(y, screen - h));
    }

    /** Moves the element so its top-left is at (x, y), re-anchoring to the nearest side. */
    public void setPosition(float x, float y) {
        float sw = screenWidth();
        float sh = screenHeight();
        float w = getScaledWidth();
        float h = getScaledHeight();
        x = Math.max(0, Math.min(x, sw - w));
        y = Math.max(0, Math.min(y, sh - h));

        float centerX = x + w / 2f;
        if (centerX < sw / 3f) {
            anchorX = Anchor.START;
            offsetX = x;
        } else if (centerX > sw * 2f / 3f) {
            anchorX = Anchor.END;
            offsetX = sw - x - w;
        } else {
            anchorX = Anchor.CENTER;
            offsetX = centerX - sw / 2f;
        }

        float centerY = y + h / 2f;
        if (centerY < sh / 3f) {
            anchorY = Anchor.START;
            offsetY = y;
        } else if (centerY > sh * 2f / 3f) {
            anchorY = Anchor.END;
            offsetY = sh - y - h;
        } else {
            anchorY = Anchor.CENTER;
            offsetY = centerY - sh / 2f;
        }
    }

    public float getScale() {
        return scale;
    }

    /** Changes the size, keeping the top-left corner in place. */
    public void setScale(float scale) {
        float x = getX();
        float y = getY();
        this.scale = Math.max(0.5f, Math.min(3f, scale));
        setPosition(x, y);
    }

    public Anchor getAnchorX() {
        return anchorX;
    }

    // ---- used by the config ----

    public void loadPosition(Anchor anchorX, Anchor anchorY, float offsetX, float offsetY, float scale) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = Math.max(0.5f, Math.min(3f, scale));
    }

    public Anchor getAnchorY() {
        return anchorY;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    private static float screenWidth() {
        return new ScaledResolution(mc).getScaledWidth();
    }

    private static float screenHeight() {
        return new ScaledResolution(mc).getScaledHeight();
    }
}
