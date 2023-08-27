package br.com.aspenmc.packet.type.member;

import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.punish.PunishType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MemberPunishRequest extends Packet {

    private UUID uniqueId;
    private UUID punisherId;

    private PunishType punishType;

    private String reason;
    private long expiresAt;

    public MemberPunishRequest(UUID uniqueId, UUID punisherId, PunishType punishType, String reason, long expiresAt) {
        bungeecord();
        this.uniqueId = uniqueId;
        this.punisherId = punisherId;
        this.punishType = punishType;
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

    public MemberPunishRequest(UUID uniqueId, Sender punisher, PunishType punishType, String reason, long expiresAt) {
        bungeecord();
        this.uniqueId = uniqueId;
        this.punisherId = punisher.getUniqueId();
        this.punishType = punishType;
        this.reason = reason;
        this.expiresAt = expiresAt;
    }

    public MemberPunishRequest(UUID uniqueId, UUID punisherId, PunishType punishType, String reason) {
        this(uniqueId, punisherId, punishType, reason, -1L);
    }
}
