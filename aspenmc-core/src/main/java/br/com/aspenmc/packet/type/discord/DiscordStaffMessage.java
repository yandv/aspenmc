package br.com.aspenmc.packet.type.discord;

import br.com.aspenmc.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DiscordStaffMessage extends Packet {

    private String discriminator;

    private String message;
}
