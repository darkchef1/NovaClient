package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.ModeSetting;
import com.nova.setting.NumberSetting;
import com.nova.util.ServerUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Says "gg" for you when a game ends on Hypixel or Minemen. */
public class AutoGG extends Module {
    /** Lines that only show up when a game ends. */
    private static final String[] END_OF_GAME = {
            "1st Killer - ", "1st Place - ", "Winner: ", "Winners: ", "Winning Team - ", " - Damage Dealt - ",
            "Match Results", "won the match", "WINNER!", "Winning Team:"
    };
    private static final long COOLDOWN_MS = 10_000;

    private final ModeSetting message = add(new ModeSetting("Message", "gg", "gg", "GG", "gg wp", "Good game"));
    private final NumberSetting delay = add(new NumberSetting("Delay", 0.5, 0, 5, 0.5, "s"));

    private long lastSent;
    private long sendAt = -1;

    public AutoGG() {
        super("AutoGG", "Says gg for you when a game ends", Category.CHAT);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.cake);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled() || event.type == 2) return;
        String text = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText()).trim();
        if (System.currentTimeMillis() - lastSent < COOLDOWN_MS || sendAt > 0) return;
        for (String trigger : END_OF_GAME) {
            if (text.contains(trigger)) {
                sendAt = System.currentTimeMillis() + (long) (delay.get() * 1000);
                return;
            }
        }
    }

    @Override
    public void onTick() {
        if (sendAt < 0 || System.currentTimeMillis() < sendAt) return;
        sendAt = -1;
        lastSent = System.currentTimeMillis();
        // on Hypixel, /achat makes sure it goes to everyone even if you're in party chat
        String prefix = ServerUtil.isHypixel() ? "/achat " : "";
        mc.thePlayer.sendChatMessage(prefix + message.getMode());
    }
}
