package com.nova.cosmetic;

/** One cosmetic in the catalog, like the Galaxy cape or Angel wings. */
public final class Cosmetic {
    public final String id;
    public final String name;
    public final CosmeticSlot slot;
    public final boolean animated;

    public Cosmetic(String id, String name, CosmeticSlot slot, boolean animated) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.animated = animated;
    }
}
