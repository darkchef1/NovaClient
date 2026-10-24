package com.nova.gui;

import com.nova.util.Anim;

import java.util.HashMap;
import java.util.Map;

/** Shared colors and helpers for the menus. The accent color comes from NovaClient.accent(). */
public final class Ui {
    public static final int PANEL = 0xE60F0F14;
    public static final int SURFACE = 0xFF1A1A22;
    public static final int SURFACE_HOVER = 0xFF24242E;
    public static final int SURFACE_DARK = 0xFF14141A;
    public static final int ON = 0xFF2EBD6B;
    public static final int OFF = 0xFFC93A4A;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFF8E8E9C;
    public static final int TRACK = 0xFF3A3A46;

    private Ui() {
    }

    public static boolean isInside(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    /** Remembers one hover animation per UI element, so hovering fades in and out. */
    public static final class Hovers {
        private final Map<Object, Anim> anims = new HashMap<>();

        public float get(Object key, boolean hovered) {
            Anim anim = anims.get(key);
            if (anim == null) {
                anim = new Anim(14f, 0f);
                anims.put(key, anim);
            }
            anim.setTarget(hovered ? 1f : 0f);
            return anim.get();
        }

        /** Same as {@link #get}, but for a state that isn't hover (like a switch being on). */
        public float state(Object key, boolean on) {
            Anim anim = anims.get(key);
            if (anim == null) {
                anim = new Anim(14f, on ? 1f : 0f);
                anims.put(key, anim);
            }
            anim.setTarget(on ? 1f : 0f);
            return anim.get();
        }
    }
}
