package com.github.writingbettercodethanyou.gamerpluginframework.minigame;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public abstract class TimedGameState extends GameState {

    private final JavaPlugin plugin;

    private BukkitTask timeDecrementTask;
    private int remainingTime;

    public TimedGameState(Game game, JavaPlugin plugin, int lengthInSeconds) {
        super(game);
        this.plugin = plugin;
        this.remainingTime = lengthInSeconds;
    }

    protected GameState getNextState() {
        return null;
    }

    protected void onSecond(int remainingTime) {
        updateScoreboards();
    }

    @Override
    public void onSwitchTo(Map<Player, PlayerState> playerStateMap) {
        this.timeDecrementTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (--remainingTime <= 0) {
                game.setState(getNextState());
                timeDecrementTask.cancel();
                timeDecrementTask = null;
            } else {
                onSecond(remainingTime);
                updateScoreboards();
            }
        }, 20L, 20L);
    }

    @Override
    public void onSwitchFrom(Class<? extends GameState> nextStateType) {
        if (timeDecrementTask != null)
            timeDecrementTask.cancel();
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public String getRemainingTimeStamp() {
        int timeToStart = getRemainingTime(), minutes, seconds;
        if (timeToStart >= 60)
            minutes = timeToStart / 60;
        else
            minutes = 0;
        seconds = timeToStart - (minutes * 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
