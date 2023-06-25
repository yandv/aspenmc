package br.com.aspenmc.punish;

import br.com.aspenmc.CommonPlugin;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Punish {

    private final String punishId;

    private final UUID playerId;
    private final UUID punisherId;

    private final PunishType punishType;

    private String reason;

    private long createdAt;
    private long expiresAt;
    private long duration;

    private UUID abrogatorId;
    private String revokedReason;
    private long revokedAt;
    private boolean revoked;


    public Punish(String punishId, UUID playerId, UUID punisherId, PunishType punishType, String reason, long expiresAt) {
        this.punishId = punishId;
        this.playerId = playerId;
        this.punisherId = punisherId;
        this.punishType = punishType;
        this.reason = reason;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = expiresAt - createdAt <= 0 ? -1 : expiresAt;
        this.duration = expiresAt - createdAt <= 0 ? -1 : this.expiresAt - createdAt;
    }

    public Punish(String punishId, UUID playerId, UUID punisherId, PunishType punishType, String reason) {
        this(punishId, playerId, punisherId, punishType, reason, -1L);
    }

    public boolean hasExpired() {
        return this.expiresAt != -1 && System.currentTimeMillis() >= this.expiresAt;
    }

    public void revoke(UUID abrogatorId, String revokedReason) {
        this.abrogatorId = abrogatorId;
        this.revokedReason = revokedReason;
        this.revokedAt = System.currentTimeMillis();
        this.revoked = true;
        CommonPlugin.getInstance().getPunishData()
                    .updatePunish(this, "abrogatorId", "revokedReason", "revokedAt", "revoked");
    }

    public boolean isPermanent() {
        return this.expiresAt == -1;
    }

    public String getPunishMessage() {
        return "";
    }
}
