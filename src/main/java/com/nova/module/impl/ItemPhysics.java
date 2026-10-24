package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Dropped items lie flat on the ground and tumble while falling, instead of floating and spinning.
 * Applied by MixinRenderEntityItem, which hands item placement to {@link #transform}.
 */
public class ItemPhysics extends Module {
    private final NumberSetting spinSpeed = add(new NumberSetting("Spin speed in air", 1, 0, 3, 0.25, "x"));

    public ItemPhysics() {
        super("Item Physics", "Dropped items lie flat and tumble", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.gold_ingot);
    }

    /**
     * Places a dropped item for drawing. Replaces vanilla's float-and-spin.
     * @return how many copies to draw for the stack, like vanilla
     */
    public static int transform(EntityItem item, double x, double y, double z, float partialTicks, IBakedModel model) {
        ItemStack stack = item.getEntityItem();
        if (stack == null || stack.getItem() == null) return 0;
        boolean block = model.isGui3d();
        float scaleY = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
        float facing = item.hoverStart * (180f / (float) Math.PI);
        float speed = NovaClient.get(ItemPhysics.class).spinSpeed.getFloat();

        if (item.onGround) {
            float lift = block ? scaleY * 0.5f : 0.03f;
            GlStateManager.translate((float) x, (float) y + lift, (float) z);
            GlStateManager.rotate(facing, 0f, 1f, 0f);
            if (!block) GlStateManager.rotate(-90f, 1f, 0f, 0f);
        } else {
            float spin = (item.getAge() + partialTicks) * 12f * speed;
            GlStateManager.translate((float) x, (float) y + 0.25f * scaleY + 0.05f, (float) z);
            GlStateManager.rotate(facing, 0f, 1f, 0f);
            GlStateManager.rotate(spin, 1f, 0f, 0f);
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
        return copies(stack.stackSize);
    }

    private static int copies(int size) {
        if (size > 48) return 5;
        if (size > 32) return 4;
        if (size > 16) return 3;
        if (size > 1) return 2;
        return 1;
    }
}
