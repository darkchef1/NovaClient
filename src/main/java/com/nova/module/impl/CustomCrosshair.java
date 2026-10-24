package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.Render;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Replaces the vanilla crosshair with your own shape, size and color. */
public class CustomCrosshair extends Module {
    private final ModeSetting style = add(new ModeSetting("Style", "Cross", "Cross", "Cross + Dot", "Dot", "Circle", "T-Shape"));
    private final NumberSetting length = add(new NumberSetting("Length", 4, 1, 10, 0.5));
    private final NumberSetting gap = add(new NumberSetting("Gap", 2, 0, 8, 0.5));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", 1, 0.5, 3, 0.5));
    private final ModeSetting color = add(new ModeSetting("Color", "White",
            "White", "Accent", "Green", "Aqua", "Yellow", "Red", "Pink", "Chroma"));
    private final BooleanSetting outline = add(new BooleanSetting("Black outline", true));
    private final BooleanSetting playerColor = add(new BooleanSetting("Red when aiming at a player", true));

    public CustomCrosshair() {
        super("Custom Crosshair", "Your own crosshair shape, size and color", Category.PVP);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.arrow);
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Pre event) {
        if (!isEnabled() || event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;
        event.setCanceled(true);
        if (mc.gameSettings.thirdPersonView != 0) return;

        float cx = event.resolution.getScaledWidth() / 2f;
        float cy = event.resolution.getScaledHeight() / 2f;
        int c = currentColor();
        if (outline.isOn()) draw(cx, cy, 0xFF000000, 0.5f);
        draw(cx, cy, c, 0f);
    }

    private int currentColor() {
        MovingObjectPosition hover = mc.objectMouseOver;
        if (playerColor.isOn() && hover != null && hover.entityHit instanceof EntityPlayer) return 0xFFFF4040;
        switch (color.getMode()) {
            case "Accent": return NovaClient.accent();
            case "Green": return 0xFF55FF55;
            case "Aqua": return 0xFF55FFFF;
            case "Yellow": return 0xFFFFFF55;
            case "Red": return 0xFFFF5555;
            case "Pink": return 0xFFFF55FF;
            case "Chroma": return Render.chroma(0);
            default: return 0xFFFFFFFF;
        }
    }

    /** Draws the crosshair; grow > 0 makes it a little bigger, used for the outline behind it. */
    private void draw(float cx, float cy, int c, float grow) {
        float t = thickness.getFloat() + grow * 2;
        float len = length.getFloat() + grow * 2;
        float g = gap.getFloat() - grow;
        String s = style.getMode();

        if (s.equals("Dot") || s.equals("Cross + Dot")) {
            Render.rect(cx - t / 2f - (s.equals("Dot") ? 0.5f : 0f), cy - t / 2f - (s.equals("Dot") ? 0.5f : 0f),
                    t + (s.equals("Dot") ? 1f : 0f), t + (s.equals("Dot") ? 1f : 0f), c);
        }
        if (s.equals("Circle")) {
            Render.arc(cx, cy, g + len / 2f + t / 2f, t, 0, 360, c);
            return;
        }
        if (s.equals("Dot")) return;

        Render.rect(cx - g - len, cy - t / 2f, len, t, c);   // left
        Render.rect(cx + g, cy - t / 2f, len, t, c);         // right
        Render.rect(cx - t / 2f, cy + g, t, len, c);         // bottom
        if (!s.equals("T-Shape")) Render.rect(cx - t / 2f, cy - g - len, t, len, c); // top
    }
}
