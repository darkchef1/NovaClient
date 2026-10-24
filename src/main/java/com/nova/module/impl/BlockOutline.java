package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.Render;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** A thicker, colored outline (and optional fill) on the block you're looking at. */
public class BlockOutline extends Module {
    private final ModeSetting color = add(new ModeSetting("Color", "Accent",
            "Black", "White", "Accent", "Red", "Green", "Aqua", "Pink", "Chroma"));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", 3, 1, 8, 0.5));
    private final NumberSetting opacity = add(new NumberSetting("Opacity", 0.8, 0.1, 1, 0.05));
    private final BooleanSetting fill = add(new BooleanSetting("Fill", false));

    public BlockOutline() {
        super("Block Outline", "Colored outline on the block you look at", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.glass);
    }

    private int rgb() {
        switch (color.getMode()) {
            case "Black": return 0x000000;
            case "White": return 0xFFFFFF;
            case "Red": return 0xFF4040;
            case "Green": return 0x40FF60;
            case "Aqua": return 0x40E0FF;
            case "Pink": return 0xFF50C0;
            case "Chroma": return Render.chroma(0) & 0xFFFFFF;
            default: return NovaClient.accent() & 0xFFFFFF;
        }
    }

    @SubscribeEvent
    public void onHighlight(DrawBlockHighlightEvent event) {
        if (!isEnabled() || event.target == null || event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        event.setCanceled(true);

        BlockPos pos = event.target.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (block.getMaterial() == Material.air || !mc.theWorld.getWorldBorder().contains(pos)) return;
        block.setBlockBoundsBasedOnState(mc.theWorld, pos);

        EntityPlayer p = event.player;
        float pt = event.partialTicks;
        double dx = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt;
        double dy = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt;
        double dz = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt;
        AxisAlignedBB box = block.getSelectedBoundingBox(mc.theWorld, pos).expand(0.002, 0.002, 0.002).offset(-dx, -dy, -dz);

        int rgb = rgb();
        float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        if (fill.isOn()) {
            GlStateManager.color(r, g, b, opacity.getFloat() * 0.3f);
            drawFilled(box);
        }
        GL11.glLineWidth(thickness.getFloat());
        GlStateManager.color(r, g, b, opacity.getFloat());
        RenderGlobal.drawSelectionBoundingBox(box);

        GL11.glLineWidth(1f);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void drawFilled(AxisAlignedBB b) {
        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        // bottom, top, north, south, west, east
        wr.pos(b.minX, b.minY, b.minZ).endVertex(); wr.pos(b.maxX, b.minY, b.minZ).endVertex(); wr.pos(b.maxX, b.minY, b.maxZ).endVertex(); wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.minZ).endVertex(); wr.pos(b.minX, b.maxY, b.maxZ).endVertex(); wr.pos(b.maxX, b.maxY, b.maxZ).endVertex(); wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.minX, b.minY, b.minZ).endVertex(); wr.pos(b.minX, b.maxY, b.minZ).endVertex(); wr.pos(b.maxX, b.maxY, b.minZ).endVertex(); wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.minX, b.minY, b.maxZ).endVertex(); wr.pos(b.maxX, b.minY, b.maxZ).endVertex(); wr.pos(b.maxX, b.maxY, b.maxZ).endVertex(); wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.minY, b.minZ).endVertex(); wr.pos(b.minX, b.minY, b.maxZ).endVertex(); wr.pos(b.minX, b.maxY, b.maxZ).endVertex(); wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.minZ).endVertex(); wr.pos(b.maxX, b.maxY, b.minZ).endVertex(); wr.pos(b.maxX, b.maxY, b.maxZ).endVertex(); wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        Tessellator.getInstance().draw();
    }
}
