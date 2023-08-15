package br.com.aspenmc.packet.type.server.clan;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;

import java.util.UUID;

public class ClanDelete extends Packet {

    private UUID clanId;

    @Override
    public void receive() {
        CommonPlugin.getInstance().getClanManager().unloadClan(clanId);
    }
}
