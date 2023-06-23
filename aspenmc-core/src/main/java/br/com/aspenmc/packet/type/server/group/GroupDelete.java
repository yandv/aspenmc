package br.com.aspenmc.packet.type.server.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;

@Getter
@AllArgsConstructor
public class GroupDelete extends Packet {

    private String groupName;

    @Override
    public void receive() {
        CommonPlugin.getInstance().getPermissionManager().unloadGroup(groupName);
    }
}
