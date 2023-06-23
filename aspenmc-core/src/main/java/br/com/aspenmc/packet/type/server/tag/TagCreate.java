package br.com.aspenmc.packet.type.server.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.permission.Tag;

@AllArgsConstructor
@Getter
public class TagCreate extends Packet {

    private Tag tag;

    @Override
    public void receive() {
        CommonPlugin.getInstance().getPermissionManager().loadTag(tag);
    }
}
