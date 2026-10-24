package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

/** A compass strip like in Fortnite or Skyrim: the direction you face is in the middle. */
public class DirectionHud extends HudModule {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 16;
    /** How many degrees fit across the strip. */
    private static final float VIEW = 120f;
    private static final String[] LABELS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    private final BooleanSetting background = add(new BooleanSetting("Background", true));
    private final BooleanSetting showDegrees = add(new BooleanSetting("Show degrees", false));

    public DirectionHud() {
        super("Direction", "A compass strip showing where you face", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.CENTER, Anchor.START, 0, 22);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.map);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(boolean preview) {
        float yaw = mc.thePlayer == null ? 180f : MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);
        if (background.isOn()) Gui.drawRect(0, 0, WIDTH, HEIGHT, 0x6F000000);

        // Minecraft's yaw 0 faces south; every 45 degrees is the next label
        for (int i = 0; i < 16; i++) {
            float angle = i * 22.5f;
            float diff = MathHelper.wrapAngleTo180_float(angle - yaw);
            if (Math.abs(diff) > VIEW / 2f) continue;
            float x = WIDTH / 2f + diff / VIEW * WIDTH;
            int alpha = (int) (255 * (1f - Math.abs(diff) / (VIEW / 2f) * 0.7f));
            if (i % 2 == 0) {
                String label = LABELS[i / 2];
                boolean north = label.equals("N");
                int color = (alpha << 24) | (north ? 0xFF5555 : 0xFFFFFF);
                mc.fontRendererObj.drawStringWithShadow(label, x - mc.fontRendererObj.getStringWidth(label) / 2f, 4, color);
            } else {
                Gui.drawRect((int) x, 6, (int) x + 1, 10, (alpha / 2 << 24) | 0xFFFFFF);
            }
        }
        // the marker in the middle
        Gui.drawRect(WIDTH / 2, 0, WIDTH / 2 + 1, 3, NovaClient.accent());
        Gui.drawRect(WIDTH / 2, HEIGHT - 3, WIDTH / 2 + 1, HEIGHT, NovaClient.accent());
        if (showDegrees.isOn()) {
            String degrees = Math.round((yaw + 360) % 360) + "°";
            mc.fontRendererObj.drawStringWithShadow(degrees, WIDTH - mc.fontRendererObj.getStringWidth(degrees) - 3, 4, 0xFFAAAAAA);
        }
    }
}
