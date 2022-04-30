package com.github.writingbettercodethanyou.gamerpluginframework.minigame;

import com.github.writingbettercodethanyou.gamerpluginframework.inject.ServiceRegistry;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public final class Game implements Listener {

    private final ServiceRegistry serviceRegistry;
    private final GameState defaultState;

    private final Map<Player, PlayerState> playerStateMap = new WeakHashMap<>();
    private GameState state;
    private boolean running;

    public Game(ServiceRegistry serviceRegistry, Class<? extends GameState> defaultState) {
        this(serviceRegistry, serviceRegistry.getInstance(defaultState));
    }

    public Game(ServiceRegistry serviceRegistry, GameState defaultState) {
        this.serviceRegistry = serviceRegistry;
        this.defaultState = defaultState;
    }

    public void broadcast(String message) {
        final String finalMessage = ChatColor.translateAlternateColorCodes('&', message);
        playerStateMap.forEach((player, state) -> player.sendMessage(finalMessage));
    }

    public void addPlayer(Player player) {
        if (hasPlayer(player))
            return;
        PlayerState playerState = new PlayerState(player.getUniqueId());
        playerStateMap.put(player, playerState);
        if (state != null) {
            state.onEntry(player, playerState);
            state.onJoin(player, playerState);
        }
    }

    public void removePlayer(Player player) {
        if (hasPlayer(player)) {
            boolean removeState = true;
            if (state != null) {
                removeState = state.onLeave(player);
                state.updateScoreboards();
            }

            if (removeState)
                playerStateMap.remove(player);
        }
    }

    public boolean hasPlayer(Player player) {
        return playerStateMap.containsKey(player);
    }

    public Set<Player> getAlivePlayers() {
        Set<Player> players = new HashSet<>();
        playerStateMap.forEach((player, state) -> {
            if (!state.isSpectator())
                players.add(player);
        });
        return players;
    }

    public Set<Player> getSpectators() {
        Set<Player> players = new HashSet<>();
        playerStateMap.forEach((player, state) -> {
            if (state.isSpectator())
                players.add(player);
        });
        return players;
    }

    public Set<Player> getPlayers() {
        return new HashSet<>(playerStateMap.keySet());
    }

    public PlayerState getPlayerState(Player player) {
        return playerStateMap.get(player);
    }

    public Map<Player, PlayerState> getPlayerStateMap() {
        return new HashMap<>(playerStateMap);
    }

    public void start() {
        if (running)
            throw new IllegalStateException("already running");
        running = true;
        state = defaultState;
        if (state != null) {
            state.onSwitchTo(getPlayerStateMap());
            state.updateScoreboards();
        }
    }

    public void stop() {
        if (!running)
            throw new IllegalStateException("already stopped");
        running = false;
        HandlerList.unregisterAll(state);
        state.onSwitchFrom(null);
        state = null;
    }

    public boolean isRunning() {
        return running;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        if (!running)
            throw new IllegalStateException("not running");
        if (state == null) {
            stop();
            return;
        }
        if (this.state != null) {
            this.state.onSwitchFrom(state.getClass());
            HandlerList.unregisterAll(this.state);
        }
        this.state = state;
        state.onSwitchTo(getPlayerStateMap());
        playerStateMap.forEach(state::onEntry);
        state.updateScoreboards();
    }

    public void setState(Class<? extends GameState> state) {
        if (state == null)
            setState((GameState) null);
        else
            setState(serviceRegistry.getInstance(state));
    }
}
