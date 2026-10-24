package com.nova.event;

import com.nova.NovaClient;
import com.nova.gui.ClickGuiScreen;
import com.nova.gui.HudEditorScreen;
import com.nova.gui.NovaMainMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import com.nova.module.HudModule;
import com.nova.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/** Drives the modules: ticks, mod keybinds, the menu key and HUD drawing. */
public final class ClientEvents {
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        while (NovaClient.MENU_KEY.isPressed()) {
            mc.displayGuiScreen(new ClickGuiScreen());
        }

        if (mc.thePlayer == null || mc.theWorld == null) return;
        for (Module module : NovaClient.getModuleManager().getModules()) {
            if (module.isEnabled()) module.onTick();
        }
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) return;
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE) return;

        boolean changed = false;
        for (Module module : NovaClient.getModuleManager().getModules()) {
            if (module.getKey() == key) {
                module.toggle();
                changed = true;
            }
        }
        if (changed) NovaClient.saveConfig();
    }

    /** Shows Nova's title screen instead of the vanilla one. */
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu && NovaClient.getSettings().customMainMenu.isOn()) {
            event.gui = new NovaMainMenu();
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.gameSettings.showDebugInfo && !NovaClient.getSettings().hudInF3.isOn()) return;
        if (mc.currentScreen instanceof HudEditorScreen) return;
        if (mc.currentScreen != null && !NovaClient.getSettings().hudInMenus.isOn()) return;

        for (HudModule module : NovaClient.getModuleManager().getHudModules()) {
            if (module.isEnabled()) renderHud(module, module.getX(), module.getY(), false);
        }
    }

    /** Draws a HUD mod at a position with its scale applied. */
    public static void renderHud(HudModule module, float x, float y, boolean preview) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(module.getScale(), module.getScale(), 1f);
        module.render(preview);
        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
