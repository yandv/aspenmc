package br.com.aspenmc.packet.type.member.teleport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.packet.Packet;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class MemberTeleportResponse extends Packet {

    private UUID uniqueId;

    private UUID targetId;
    private String targetName;

    private String serverId;

    private boolean success;



}
