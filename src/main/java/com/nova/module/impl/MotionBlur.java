package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.NumberSetting;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Leaves a short trail as the camera moves, using Minecraft's built-in "phosphor" shader.
 * Doesn't work with OptiFine's Fast Render turned on.
 */
public class MotionBlur extends Module {
    private static final ResourceLocation SHADER = new ResourceLocation("shaders/post/phosphor.json");
    private static Field listShaders;

    private final NumberSetting strength = add(new NumberSetting("Strength", 5, 1, 10, 1));
    private boolean applied;

    public MotionBlur() {
        super("Motion Blur", "Smooth trail when you move your camera", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.feather);
    }

    @Override
    public void onTick() {
        if (!OpenGlHelper.shadersSupported || mc.currentScreen != null) return;
        if (!mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.loadShader(SHADER);
            applied = true;
        }
        if (applied) setStrength(mc.entityRenderer.getShaderGroup());
    }

    @Override
    public void onDisable() {
        if (applied && mc.entityRenderer.isShaderActive()) mc.entityRenderer.stopUseShader();
        applied = false;
    }

    /** The phosphor shader blends each frame with the last one; a higher value keeps more of the old frame. */
    @SuppressWarnings("unchecked")
    private void setStrength(ShaderGroup group) {
        if (group == null) return;
        float amount = 0.5f + strength.getFloat() * 0.045f;
        try {
            for (Shader shader : shaders(group)) {
                ShaderUniform uniform = shader.getShaderManager().getShaderUniform("Phosphor");
                if (uniform != null) uniform.set(amount, amount, amount);
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Shader> shaders(ShaderGroup group) throws IllegalAccessException {
        if (listShaders == null) {
            for (Field field : ShaderGroup.class.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                List<?> list = (List<?>) field.get(group);
                if (!list.isEmpty() && list.get(0) instanceof Shader) {
                    listShaders = field;
                    break;
                }
            }
            if (listShaders == null) throw new IllegalAccessException("shader list not found");
        }
        return (List<Shader>) listShaders.get(group);
    }
}
