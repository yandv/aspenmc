package br.com.aspenmc.packet.type.member.skin;

import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.packet.Packet;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SkinChangeRequest extends Packet {

    private final UUID playerId;
    private final Skin skin;

    public SkinChangeRequest(UUID playerId, Skin skin) {
        this.playerId = playerId;
        this.skin = skin;
        bungeecord();
    }

}
