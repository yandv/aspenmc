package br.com.aspenmc.punish;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.utils.string.StringFormat;
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


    public Punish(String punishId, UUID playerId, UUID punisherId, PunishType punishType, String reason,
            long expiresAt) {
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

    public String getPunishMessage(Language language) {
        switch (punishType) {
        case BAN:
            if (isPermanent()) {
                return language.t("ban-permanent-message",
                        "§cVocê foi permanentemente banido do servidor.\n§cMotivo: %reason%\n§c\n§eSaiba mais em " +
                                "§bwww.aspenmc.com.br", "%reason%", reason, "%punisher%", punisherId.toString(),
                        "%expiresAt%", "nunca", "%createdAt%", CommonConst.DATE_FORMAT.format(createdAt), "%duration%",
                        "eterno");
            } else {
                return language.t("ban-temporary-message",
                        "§cVocê foi temporariamente banido do servidor.\n§cMotivo: %reason%\n§cExpira em: " +
                                "%expiresAt%\n§c\n§eSaiba mais em " +
                                "§bwww.aspenmc.com.br", "%reason%", reason, "%punisher%", punisherId.toString(),
                        "%expiresAt%", StringFormat.formatTime((System.currentTimeMillis() - expiresAt) / 1000),
                        "%createdAt%", CommonConst.DATE_FORMAT.format(createdAt), "%duration%", "eterno");
            }
        case MUTE:
            if (isPermanent()) {
                return language.t("mute-permanent-message",
                        "§cVocê foi silenciado permanentemente por %reason%.", "%reason%", reason, "%punisher%", punisherId.toString(),
                        "%expiresAt%", "nunca", "%createdAt%", CommonConst.DATE_FORMAT.format(createdAt), "%duration%",
                        "eterno");
            } else {
                return language.t("mute-temporary-message",
                        "§cVocê foi silenciado temporariamente por %reason%, expira em %expiresAt%.", "%reason%", reason, "%punisher%", punisherId.toString(),
                        "%expiresAt%", StringFormat.formatTime((System.currentTimeMillis() - expiresAt) / 1000),
                        "%createdAt%", CommonConst.DATE_FORMAT.format(createdAt), "%duration%", "eterno");
            }
        }

        return "";
    }
}
