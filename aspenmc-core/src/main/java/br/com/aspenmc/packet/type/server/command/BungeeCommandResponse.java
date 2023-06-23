package br.com.aspenmc.packet.type.server.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.packet.Packet;

@Getter
public class BungeeCommandResponse extends Packet {

    private final NormalCommand[] commands;

    public BungeeCommandResponse(NormalCommand... commands) {
        this.commands = commands;
    }

    @AllArgsConstructor
    @Getter
    public static class NormalCommand {

        private String name;
        private String[] aliases;
        private String permission;

    }
}
