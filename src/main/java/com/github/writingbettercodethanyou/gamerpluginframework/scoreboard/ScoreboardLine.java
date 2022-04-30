package com.github.writingbettercodethanyou.gamerpluginframework.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

final class ScoreboardLine {

    private final Team team;

    ScoreboardLine(Objective objective, int lineNumber) {
        String entryName = ChatColor.values()[lineNumber].toString();
        Team team = objective.getScoreboard().getTeam(Integer.toString(lineNumber));
        if (team == null)
            team = objective.getScoreboard().registerNewTeam(Integer.toString(lineNumber));
        team.setPrefix("");
        team.setSuffix("");
        team.setDisplayName(entryName);
        team.addEntry(entryName);
        objective.getScore(entryName).setScore(16 - lineNumber);
        this.team = team;
    }

    void setText(String text) {
        if (text == null) {
            team.getScoreboard().resetScores(team.getDisplayName());
            return;
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        String prefix, suffix = "";
        if (text.length() > 16) {
            prefix = text.substring(0, 16);
            suffix = ChatColor.getLastColors(prefix) + text.substring(16);
            if (suffix.length() > 16)
                suffix = suffix.substring(0, 16);
        } else
            prefix = text;

        if (!team.getPrefix().equals(prefix))
            team.setPrefix(prefix);
        if (!team.getSuffix().equals(suffix))
            team.setSuffix(suffix);
    }
}
