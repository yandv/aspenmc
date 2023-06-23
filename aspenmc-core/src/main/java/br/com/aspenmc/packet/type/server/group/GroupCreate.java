package br.com.aspenmc.packet.type.server.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.permission.Group;

@AllArgsConstructor
@Getter
public class GroupCreate extends Packet {

    private Group group;

    @Override
    public void receive() {
        CommonPlugin.getInstance().getPermissionManager().loadGroup(group);
    }
}
