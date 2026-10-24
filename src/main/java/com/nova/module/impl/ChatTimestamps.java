package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/** Puts the time in front of every chat message, like "[6:04 PM]". */
public class ChatTimestamps extends Module {
    /** Matches a timestamp this mod added, so Compact Chat can ignore it when comparing messages. */
    public static final Pattern PREFIX = Pattern.compile("^\\[\\d{1,2}:\\d{2}(:\\d{2})?( [AP]M)?] ");

    private final BooleanSetting twentyFourHour = add(new BooleanSetting("24-hour time", false));
    private final BooleanSetting seconds = add(new BooleanSetting("Show seconds", false));

    public ChatTimestamps() {
        super("Chat Timestamps", "Shows the time before each chat message", Category.CHAT);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.clock);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled() || event.type == 2) return;
        String pattern = twentyFourHour.isOn() ? "HH:mm" : "h:mm";
        if (seconds.isOn()) pattern += ":ss";
        if (!twentyFourHour.isOn()) pattern += " a";
        String time = new SimpleDateFormat(pattern).format(new Date());
        event.message = new ChatComponentText("§7[" + time + "] §r").appendSibling(event.message);
    }
}
