package com.nova.cosmetic;

import com.nova.NovaClient;
import com.nova.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Puts a small Nova star in front of the name of other Nova players, and adds the cosmetics
 * layer to Minecraft's player renderers once the game is ready.
 */
public final class NovaBadge {
    private static final float NAME_SCALE = 0.026666668f;
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean layersAdded;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (layersAdded || event.phase != TickEvent.Phase.END || mc.getRenderManager() == null) return;
        for (RenderPlayer renderer : mc.getRenderManager().getSkinMap().values()) {
            renderer.addLayer(new CosmeticLayer());
        }
        layersAdded = true;
    }

    @SubscribeEvent
    public void onNameTag(RenderLivingEvent.Specials.Post<EntityLivingBase> event) {
        if (!(event.entity instanceof EntityPlayer) || event.entity == mc.thePlayer) return;
        EntityPlayer player = (EntityPlayer) event.entity;
        if (player.isInvisible() || player.isSneaking() || !Cosmetics.isNovaPlayer(player)) return;
        double distanceSq = player.getDistanceSqToEntity(mc.getRenderViewEntity());
        if (distanceSq > 64 * 64) return;

        // match where Minecraft puts the name, including the extra line some servers show under it
        float y = player.height + 0.5f;
        if (distanceSq < 100 && mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(2) != null) {
            y += mc.fontRendererObj.FONT_HEIGHT * 1.15f * NAME_SCALE;
        }
        int nameWidth = mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText());

        RenderManager rm = mc.getRenderManager();
        GlStateManager.pushMatrix();
        GlStateManager.translate(event.x, event.y + y, event.z);
        GlStateManager.rotate(-rm.playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(rm.playerViewX, 1f, 0f, 0f);
        GlStateManager.scale(-NAME_SCALE, -NAME_SCALE, NAME_SCALE);
        GlStateManager.disableLighting();
        float x = -nameWidth / 2f - 8f;
        Render.circle(x, 3.5f, 5.5f, 0x55000000);
        Render.star(x, 3.5f, 4.5f, 1.1f, 4, 0f, NovaClient.accent());
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
