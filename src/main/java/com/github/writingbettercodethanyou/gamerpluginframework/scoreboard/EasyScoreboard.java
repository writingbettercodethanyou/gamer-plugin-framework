package com.github.writingbettercodethanyou.gamerpluginframework.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

// TODO 2022/04/30: fix scoreboard bugging out, maybe use packets instead?
public final class EasyScoreboard {

    private static final Map<Player, EasyScoreboard> PLAYER_SCOREBOARD_MAP = new HashMap<>();

    public static EasyScoreboard createScoreboard(Player player, String title) {
        if (PLAYER_SCOREBOARD_MAP.containsKey(player))
            return PLAYER_SCOREBOARD_MAP.get(player);
        EasyScoreboard scoreboard = new EasyScoreboard(player, title);
        PLAYER_SCOREBOARD_MAP.put(player, scoreboard);
        return scoreboard;
    }

    public static EasyScoreboard getScoreboard(Player player) {
        return PLAYER_SCOREBOARD_MAP.get(player);
    }

    private final Player player;
    private final Objective objective;
    private final ScoreboardLine[] lines;

    EasyScoreboard(Player player, String title) {
        this.player = player;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        Objective objective = scoreboard.getObjective("scoreboard");
        if (objective == null)
            objective = scoreboard.registerNewObjective("scoreboard", "dummy", ChatColor.translateAlternateColorCodes('&', title), RenderType.INTEGER);
        else
            objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective = objective;
        this.lines = new ScoreboardLine[16];
    }

    public Player getPlayer() {
        return player;
    }

    public EasyScoreboard setTitle(String title) {
        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
        return this;
    }

    public EasyScoreboard setLine(int line, String text) {
        if (line < 1 || line > 16)
            throw new IndexOutOfBoundsException("line number must be between 1 and 16 inclusive");
        line--;
        for (int i = line; i >= 0 && lines[i] == null; i--)
            lines[i] = new ScoreboardLine(objective, i + 1);
        lines[line].setText(text);
        return this;
    }

    public EasyScoreboard removeLine(int line) {
        line--;
        if (lines[line] == null)
            return this;
        lines[line].setText(null);
        return this;
    }

    public void refresh() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.setScoreboard(objective.getScoreboard());
    }
}
