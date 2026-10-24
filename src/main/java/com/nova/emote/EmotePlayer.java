package com.nova.emote;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;

/**
 * Plays an emote on your own player: blends the pose in and out, turns the camera around
 * so you can see it, and stops when you move or attack.
 */
public final class EmotePlayer {
    /** Ticks spent blending into and out of the pose. */
    private static final float BLEND = 5f;

    private static Emote current;
    private static long startedAt;
    private static int savedView = -1;

    private EmotePlayer() {
    }

    public static void play(Emote emote, boolean turnCamera) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (current == null && turnCamera) {
            savedView = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 2;
        }
        current = emote;
        startedAt = System.currentTimeMillis();
    }

    public static void stop() {
        if (current == null) return;
        current = null;
        if (savedView >= 0) Minecraft.getMinecraft().gameSettings.thirdPersonView = savedView;
        savedView = -1;
    }

    public static boolean isPlaying() {
        return current != null;
    }

    private static float elapsedTicks() {
        return (System.currentTimeMillis() - startedAt) / 50f;
    }

    /** Called every tick: ends the emote when it's over or when you move, jump, sneak or click. */
    public static void tick() {
        if (current == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        GameSettings s = mc.gameSettings;
        boolean acting = s.keyBindForward.isKeyDown() || s.keyBindBack.isKeyDown() || s.keyBindLeft.isKeyDown()
                || s.keyBindRight.isKeyDown() || s.keyBindJump.isKeyDown() || s.keyBindSneak.isKeyDown()
                || s.keyBindAttack.isKeyDown() || s.keyBindUseItem.isKeyDown();
        boolean over = !current.loops && elapsedTicks() > current.duration;
        if (mc.thePlayer == null || mc.thePlayer.isDead || (acting && mc.currentScreen == null) || over) stop();
    }

    /** How much of the emote shows right now: fades in at the start and out at the end. */
    private static float blend(float t) {
        float blend = Math.min(1f, t / BLEND);
        if (!current.loops) blend = Math.min(blend, Math.max(0f, (current.duration - t) / BLEND));
        return blend;
    }

    /** Moves or turns the whole body, for emotes like headspins (see MixinRenderPlayer). */
    public static void moveBody(Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        if (current == null || entity != mc.thePlayer || mc.gameSettings.thirdPersonView == 0) return;
        float t = elapsedTicks();
        current.body(t, blend(t));
    }

    /** Applies the emote to a player model after Minecraft has posed it (see MixinModelPlayer). */
    public static void apply(ModelPlayer model, Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        if (current == null || entity != mc.thePlayer || mc.gameSettings.thirdPersonView == 0) return;

        float t = elapsedTicks();
        float blend = blend(t);

        ModelRenderer[] parts = {model.bipedHead, model.bipedBody, model.bipedRightArm, model.bipedLeftArm, model.bipedRightLeg, model.bipedLeftLeg};
        float[][] before = new float[parts.length][];
        for (int i = 0; i < parts.length; i++) before[i] = new float[]{parts[i].rotateAngleX, parts[i].rotateAngleY, parts[i].rotateAngleZ};

        current.pose(model, t);
        for (int i = 0; i < parts.length; i++) {
            ModelRenderer p = parts[i];
            p.rotateAngleX = before[i][0] + (p.rotateAngleX - before[i][0]) * blend;
            p.rotateAngleY = before[i][1] + (p.rotateAngleY - before[i][1]) * blend;
            p.rotateAngleZ = before[i][2] + (p.rotateAngleZ - before[i][2]) * blend;
        }
        copyToOuterLayer(model);
    }

    /** The outer skin layer (sleeves, jacket, hat) follows the parts underneath. */
    private static void copyToOuterLayer(ModelPlayer m) {
        ModelBase.copyModelAngles(m.bipedHead, m.bipedHeadwear);
        ModelBase.copyModelAngles(m.bipedBody, m.bipedBodyWear);
        ModelBase.copyModelAngles(m.bipedRightArm, m.bipedRightArmwear);
        ModelBase.copyModelAngles(m.bipedLeftArm, m.bipedLeftArmwear);
        ModelBase.copyModelAngles(m.bipedRightLeg, m.bipedRightLegwear);
        ModelBase.copyModelAngles(m.bipedLeftLeg, m.bipedLeftLegwear);
    }
}
