package com.nova.font;

/** The client's fonts. Textures are created on first use. */
public final class Fonts {
    public static final NovaFont SMALL = new NovaFont(false, 6.5f);
    public static final NovaFont REGULAR = new NovaFont(false, 8f);
    public static final NovaFont MEDIUM = new NovaFont(true, 8f);
    public static final NovaFont LARGE = new NovaFont(true, 11f);
    public static final NovaFont TITLE = new NovaFont(true, 16f);
    public static final NovaFont HUGE = new NovaFont(true, 26f);

    private Fonts() {
    }
}
