package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

/**
 * 1.7 animations: swinging while blocking (blockhit), swinging while eating or drawing a bow,
 * and armor that flashes red when hit. The swings are visual only; nothing is sent to the server.
 * Applied by MixinItemRenderer and MixinLayerArmorBase.
 */
public class OldAnimations extends Module {
    private final BooleanSetting blockhit = add(new BooleanSetting("Blockhit animation", true));
    private final BooleanSetting swingWhileUsing = add(new BooleanSetting("Swing while eating / bow", false));
    private final BooleanSetting redArmor = add(new BooleanSetting("Red armor when hit", true));

    public OldAnimations() {
        super("1.7 Animations", "Old blockhit swing and red armor from 1.7", Category.MECHANIC);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.diamond_sword);
    }

    @Override
    public void onTick() {
        EntityPlayerSP player = mc.thePlayer;
        if (!player.isUsingItem() || mc.currentScreen != null || !mc.gameSettings.keyBindAttack.isKeyDown()) return;
        boolean blocking = player.getItemInUse() != null && player.getItemInUse().getItemUseAction() == EnumAction.BLOCK;
        if (blocking ? !blockhit.isOn() : !swingWhileUsing.isOn()) return;

        // start the arm swing on our screen only, like 1.7 did when you clicked while blocking
        if (!player.isSwingInProgress || player.swingProgressInt >= 3 || player.swingProgressInt < 0) {
            player.swingProgressInt = -1;
            player.isSwingInProgress = true;
        }
    }

    public static boolean blockhit() {
        return NovaClient.isEnabled(OldAnimations.class) && NovaClient.get(OldAnimations.class).blockhit.isOn();
    }

    public static boolean swingWhileUsing() {
        return NovaClient.isEnabled(OldAnimations.class) && NovaClient.get(OldAnimations.class).swingWhileUsing.isOn();
    }

    public static boolean redArmor() {
        return NovaClient.isEnabled(OldAnimations.class) && NovaClient.get(OldAnimations.class).redArmor.isOn();
    }
}
