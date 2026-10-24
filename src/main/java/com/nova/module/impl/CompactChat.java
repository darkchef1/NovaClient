package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Repeated messages stack into one line with a counter, like "Join our Discord! (x5)". */
public class CompactChat extends Module {
    private static final int FIRST_ID = 0x4E4F5600;
    private static final long WINDOW_MS = 60_000;

    private String lastText;
    private int lastId;
    private int count;
    private long lastTime;
    private int nextId = FIRST_ID;

    public CompactChat() {
        super("Compact Chat", "Stacks repeated chat messages into one line", Category.CHAT);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.paper);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled() || event.type == 2 || event.isCanceled()) return;
        String text = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        text = ChatTimestamps.PREFIX.matcher(text).replaceFirst("").trim();
        if (text.isEmpty()) return;

        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        long now = System.currentTimeMillis();
        if (text.equals(lastText) && now - lastTime < WINDOW_MS) {
            count++;
            chat.deleteChatLine(lastId);
        } else {
            count = 1;
            lastText = text;
            lastId = nextId++;
            if (nextId > FIRST_ID + 50_000) nextId = FIRST_ID;
        }
        lastTime = now;

        IChatComponent message = event.message.createCopy();
        if (count > 1) message.appendSibling(new ChatComponentText(" §7(x" + count + ")"));
        chat.printChatMessageWithOptionalDeletion(message, lastId);
        event.setCanceled(true);
    }
}
