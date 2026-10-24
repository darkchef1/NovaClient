package com.nova;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;

/** Options in the SETTINGS tab. Not a mod: it never shows in the mods grid. */
public final class ClientSettings extends Module {
    public final ModeSetting accent = add(new ModeSetting("Accent color", "Blue",
            "Blue", "Purple", "Pink", "Red", "Orange", "Green", "Aqua"));
    public final BooleanSetting menuBlur = add(new BooleanSetting("Blur behind menus", true));
    public final BooleanSetting hudInF3 = add(new BooleanSetting("Show HUD in F3", false));
    public final BooleanSetting hudInMenus = add(new BooleanSetting("Show HUD behind menus", true));
    public final BooleanSetting snapping = add(new BooleanSetting("HUD editor snapping", true));

    public ClientSettings() {
        super("Client", "Client-wide options", Category.MISC);
    }

    public int getAccent() {
        switch (accent.getMode()) {
            case "Purple": return 0xFFA86BFF;
            case "Pink": return 0xFFFF5FB4;
            case "Red": return 0xFFFF5A5A;
            case "Orange": return 0xFFFFA03C;
            case "Green": return 0xFF3DDC84;
            case "Aqua": return 0xFF3DE0D8;
            default: return 0xFF4C9DFF;
        }
    }
}
