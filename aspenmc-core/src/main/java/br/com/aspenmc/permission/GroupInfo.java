package br.com.aspenmc.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GroupInfo {

    private UUID playerId;

    private long expiresAt;
    private long duration;
    private long createdAt;
    private long lastUpdatedAt;

    private boolean defaultGroup;

    public GroupInfo(UUID playerId) {
        this(playerId, -1, -1, System.currentTimeMillis(), System.currentTimeMillis(), false);
    }

    public GroupInfo(UUID playerId, boolean defaultGroup) {
        this(playerId, -1, -1, System.currentTimeMillis(), System.currentTimeMillis(), defaultGroup);
    }

    public GroupInfo(UUID playerId, long expiresAt) {
        this(playerId, expiresAt, expiresAt <= 0 ? -1L : expiresAt - System.currentTimeMillis(),
             System.currentTimeMillis(), System.currentTimeMillis(), false);
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
        this.lastUpdatedAt = System.currentTimeMillis();
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
        this.duration = expiresAt <= 0 ? -1L : expiresAt - System.currentTimeMillis();
        this.lastUpdatedAt = System.currentTimeMillis();
    }

    public boolean isPermanent() {
        return expiresAt <= 0;
    }

    public boolean hasExpired() {
        return expiresAt != -1 && expiresAt < System.currentTimeMillis();
    }
}
