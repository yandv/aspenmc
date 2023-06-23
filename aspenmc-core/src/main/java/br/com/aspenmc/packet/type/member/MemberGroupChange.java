package br.com.aspenmc.packet.type.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.packet.Packet;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class MemberGroupChange extends Packet {

    private UUID playerId;
    private String groupName;
    private long expiresAt;
    private long duration;
    private GroupAction groupAction;

    public enum GroupAction {
        ADD,
        SET,
        REMOVE
    }
}
