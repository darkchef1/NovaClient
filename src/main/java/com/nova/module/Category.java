package com.nova.module;

public enum Category {
    HUD("HUD"),
    PVP("PvP"),
    MECHANIC("Mechanic"),
    CHAT("Chat"),
    MISC("Misc");

    public final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }
}
