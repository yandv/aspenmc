package br.com.aspenmc.packet.type.member.skin;

import br.com.aspenmc.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class SkinChangeResponse extends Packet {

    private UUID playerId;

    private SkinResult skinResult;


    public enum SkinResult {

        SUCCESS,
        UNKNOWN_ERROR
    }
}
