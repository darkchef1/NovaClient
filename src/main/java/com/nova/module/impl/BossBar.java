package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** The boss health bar (Wither, Ender Dragon, or the ones servers use for messages), movable. */
public class BossBar extends HudModule {
    private static final int BAR_W = 182;

    private final BooleanSetting showName = add(new BooleanSetting("Show name", true));
    private final BooleanSetting showBar = add(new BooleanSetting("Show bar", true));

    public BossBar() {
        super("Boss Bar", "Move the boss health bar", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.CENTER, Anchor.START, 0, 2);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.skull, 1, 1);
    }

    /** Hide Minecraft's own boss bar while this one is on. */
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Pre event) {
        if (isEnabled() && event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) event.setCanceled(true);
    }

    @Override
    public int getWidth() {
        return BAR_W;
    }

    @Override
    public int getHeight() {
        return 15;
    }

    @Override
    public void render(boolean preview) {
        boolean active = BossStatus.bossName != null && BossStatus.statusBarTime > 0;
        if (!active && !preview) return;
        String name = active ? BossStatus.bossName : "Ender Dragon";
        float health = active ? BossStatus.healthScale : 0.7f;
        // vanilla counts this down every frame it draws the bar, so the bar hides when the boss is gone
        if (active && !preview) BossStatus.statusBarTime--;

        if (showBar.isOn()) {
            mc.getTextureManager().bindTexture(Gui.icons);
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.enableBlend();
            Gui.drawModalRectWithCustomSizedTexture(0, 10, 0, 74, BAR_W, 5, 256, 256);
            int filled = (int) (health * (BAR_W + 1));
            if (filled > 0) Gui.drawModalRectWithCustomSizedTexture(0, 10, 0, 79, filled, 5, 256, 256);
        }
        if (showName.isOn()) {
            mc.fontRendererObj.drawStringWithShadow(name, (BAR_W - mc.fontRendererObj.getStringWidth(name)) / 2f, 0, 0xFFFFFFFF);
        }
    }
}
