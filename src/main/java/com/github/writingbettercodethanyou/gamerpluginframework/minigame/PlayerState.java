package com.github.writingbettercodethanyou.gamerpluginframework.minigame;

import java.util.UUID;

public class PlayerState {

    private final UUID uuid;

    private boolean spectator;
    private boolean invincible;
    private boolean pvp;
    private boolean pve;

    PlayerState(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean isInvincible() {
        return invincible || spectator;
    }

    public boolean canPVP() {
        return pvp && !spectator;
    }

    public boolean canPVE() {
        return pve && !spectator;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public void setPVP(boolean pvp) {
        this.pvp = pvp;
    }

    public void setPVE(boolean pve) {
        this.pve = pve;
    }
}
