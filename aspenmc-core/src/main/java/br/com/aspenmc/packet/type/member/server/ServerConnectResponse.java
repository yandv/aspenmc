package br.com.aspenmc.packet.type.member.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.packet.Packet;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ServerConnectResponse extends Packet {

    private UUID playerId;
    private String serverId;
    private ResponseType responseType;

    public enum ResponseType {
        SUCCESS,
        SERVER_UNAVAILABLE,
        SERVER_NOT_FOUND,
        SERVER_FULL,
        INSSUFICIENT_PERMISSIONS,
        UNKNOWN_ERROR
    }
}
