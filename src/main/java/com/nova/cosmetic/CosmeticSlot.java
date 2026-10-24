package com.nova.cosmetic;

/** Where a cosmetic goes. You can wear one of each at a time. */
public enum CosmeticSlot {
    CAPE("Capes", "cape"),
    WINGS("Wings", "wings"),
    HEAD("Head", "head"),
    AURA("Aura", "aura");

    public final String displayName;
    /** The key used in cosmetics.json files. */
    public final String key;

    CosmeticSlot(String displayName, String key) {
        this.displayName = displayName;
        this.key = key;
    }

    public static CosmeticSlot byKey(String key) {
        for (CosmeticSlot slot : values()) if (slot.key.equals(key)) return slot;
        return null;
    }
}
