package com.nova.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/** Counts mouse clicks in the last second. Shared by the CPS and Keystrokes mods. */
public final class CpsTracker {
    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!event.buttonstate) return;
        if (event.button == 0) LEFT.addLast(Minecraft.getSystemTime());
        else if (event.button == 1) RIGHT.addLast(Minecraft.getSystemTime());
    }

    public static int getLeftCps() {
        return count(LEFT);
    }

    public static int getRightCps() {
        return count(RIGHT);
    }

    private static int count(Deque<Long> clicks) {
        long cutoff = Minecraft.getSystemTime() - 1000;
        while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) clicks.pollFirst();
        return clicks.size();
    }
}
