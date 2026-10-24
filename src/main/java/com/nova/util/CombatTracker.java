package com.nova.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Watches your attacks for the Combo Counter and Reach Display.
 * A hit counts toward the combo once the target actually plays its hurt animation.
 */
public final class CombatTracker {
    private static final long HIT_CONFIRM_MS = 400;
    private static final long COMBO_RESET_MS = 2000;

    private static int combo;
    private static long lastHitTime;
    private static double lastReach = -1;
    private static long lastReachTime;

    private EntityLivingBase pendingTarget;
    private long pendingSince;
    private int lastHurtTime;

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.entityPlayer != mc.thePlayer) return;
        Entity target = event.target;

        MovingObjectPosition hover = mc.objectMouseOver;
        if (hover != null && hover.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hover.hitVec != null) {
            lastReach = mc.thePlayer.getPositionEyes(1f).distanceTo(hover.hitVec);
            lastReachTime = System.currentTimeMillis();
        }
        if (target instanceof EntityLivingBase) {
            pendingTarget = (EntityLivingBase) target;
            pendingSince = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            combo = 0;
            return;
        }
        long now = System.currentTimeMillis();

        if (pendingTarget != null) {
            if (pendingTarget.hurtTime >= pendingTarget.maxHurtTime - 2 && pendingTarget.hurtTime > 0) {
                combo++;
                lastHitTime = now;
                pendingTarget = null;
            } else if (now - pendingSince > HIT_CONFIRM_MS) {
                pendingTarget = null;
            }
        }

        // getting hit yourself ends your combo
        int hurt = mc.thePlayer.hurtTime;
        if (hurt > lastHurtTime) combo = 0;
        lastHurtTime = hurt;
        if (now - lastHitTime > COMBO_RESET_MS) combo = 0;
    }

    public static int getCombo() {
        return combo;
    }

    /** Distance of your last hit in blocks, or -1 if you haven't hit anything recently. */
    public static double getLastReach(long maxAgeMs) {
        if (lastReach < 0 || System.currentTimeMillis() - lastReachTime > maxAgeMs) return -1;
        return lastReach;
    }
}
