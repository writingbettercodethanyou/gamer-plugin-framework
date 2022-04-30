package com.github.writingbettercodethanyou.gamerpluginframework.minigame;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;

public abstract class GameState implements Listener {

    protected final Game game;

    public GameState(Game game) {
        this.game = game;
    }

    public void onEntry(Player player, PlayerState state) {
    }

    public void onJoin(Player player, PlayerState state) {
    }

    public boolean onLeave(Player player) {
        return true;
    }

    public void onSwitchTo(Map<Player, PlayerState> playerStateMap) {
    }

    public void onSwitchFrom(Class<? extends GameState> nextStateType) {
    }

    public void updateScoreboard(Player player) {
    }

    public void updateScoreboards() {
        game.getPlayers().forEach(this::updateScoreboard);
    }
}
