package com.nova.module.impl;

import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.setting.BooleanSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The server's sidebar scoreboard as a movable HUD element, with the red numbers optional.
 * While this is on, MixinGuiIngame hides Minecraft's own scoreboard.
 */
public class ScoreboardHud extends HudModule {
    private static final int LINE = 9;
    private static final int PAD = 2;

    private final BooleanSetting numbers = add(new BooleanSetting("Show red numbers", false));
    private final BooleanSetting background = add(new BooleanSetting("Background", true));

    public ScoreboardHud() {
        super("Scoreboard", "Move the scoreboard and hide its red numbers", Category.HUD, 2, 2);
        setDefaultPosition(Anchor.END, Anchor.CENTER, 2, 0);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.book);
    }

    /** The objective Minecraft would show in the sidebar: your team's color slot first, then the normal one. */
    private ScoreObjective objective() {
        if (mc.theWorld == null || mc.thePlayer == null) return null;
        Scoreboard board = mc.theWorld.getScoreboard();
        ScorePlayerTeam team = board.getPlayersTeam(mc.thePlayer.getName());
        if (team != null && team.getChatFormat() != null) {
            int color = team.getChatFormat().getColorIndex();
            if (color >= 0) {
                ScoreObjective byColor = board.getObjectiveInDisplaySlot(3 + color);
                if (byColor != null) return byColor;
            }
        }
        return board.getObjectiveInDisplaySlot(1);
    }

    /** One row: the name with team colors, and its score. */
    private static final class Row {
        final String text;
        final String score;

        Row(String text, String score) {
            this.text = text;
            this.score = score;
        }
    }

    private String title(boolean preview) {
        ScoreObjective objective = objective();
        if (objective != null) return objective.getDisplayName();
        return preview ? "§e§lNOVA" : null;
    }

    /** Rows from top to bottom, at most 15 like vanilla. */
    private List<Row> rows(boolean preview) {
        List<Row> rows = new ArrayList<>();
        ScoreObjective objective = objective();
        if (objective == null) {
            if (preview) {
                for (String s : Arrays.asList("§7Your scoreboard", "", "Kills: §a12", "Wins: §a3", "", "§ehypixel.net")) {
                    rows.add(new Row(s, Integer.toString(rows.size())));
                }
            }
            return rows;
        }
        Scoreboard board = objective.getScoreboard();
        Collection<Score> scores = board.getSortedScores(objective);
        List<Score> visible = new ArrayList<>();
        for (Score score : scores) {
            if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) visible.add(score);
        }
        if (visible.size() > 15) visible = visible.subList(visible.size() - 15, visible.size());
        for (int i = visible.size() - 1; i >= 0; i--) {
            Score score = visible.get(i);
            ScorePlayerTeam team = board.getPlayersTeam(score.getPlayerName());
            rows.add(new Row(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()),
                    EnumChatFormatting.RED + "" + score.getScorePoints()));
        }
        return rows;
    }

    @Override
    public int getWidth() {
        String title = title(true);
        int width = title == null ? 60 : mc.fontRendererObj.getStringWidth(title);
        for (Row row : rows(true)) {
            int w = mc.fontRendererObj.getStringWidth(row.text);
            if (numbers.isOn()) w += 4 + mc.fontRendererObj.getStringWidth(row.score);
            width = Math.max(width, w);
        }
        return width + PAD * 2;
    }

    @Override
    public int getHeight() {
        return (rows(true).size() + 1) * LINE + 1;
    }

    @Override
    public void render(boolean preview) {
        String title = title(preview);
        if (title == null) return;
        List<Row> rows = rows(preview);
        int width = getWidth();

        if (background.isOn()) {
            Gui.drawRect(0, 0, width, LINE + 1, 0x66000000);
            Gui.drawRect(0, LINE + 1, width, getHeight(), 0x50000000);
        }
        mc.fontRendererObj.drawStringWithShadow(title, (width - mc.fontRendererObj.getStringWidth(title)) / 2f, 1, 0xFFFFFFFF);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int y = (i + 1) * LINE + 1;
            mc.fontRendererObj.drawString(row.text, PAD, y, 0xFFFFFFFF, false);
            if (numbers.isOn()) {
                mc.fontRendererObj.drawString(row.score, width - PAD - mc.fontRendererObj.getStringWidth(row.score), y, 0xFFFFFFFF, false);
            }
        }
    }
}
