package br.com.aspenmc.packet.type.server.clan;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.packet.Packet;
import lombok.Getter;

@Getter
public class ClanCreate extends Packet {

    private Clan clan;

    @Override
    public void receive() {
        if (CommonPlugin.getInstance().getServerId().equals(getSource())) return;

        clan.getOnlineMembers().stream().findFirst().ifPresent(member -> {
            CommonPlugin.getInstance().getClanManager().loadClan(clan);
        });
    }
}
